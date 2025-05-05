package org.almagestauth.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.almagestauth.domain.entity.Member;
import org.almagestauth.domain.entity.QMember;
import org.almagestauth.domain.entity.Role;
import org.almagestauth.domain.repository.MemberRepository;
import org.almagestauth.domain.repository.RoleRepository;
import org.almagestauth.dto.*;
import org.almagestauth.exception.r400.IllegalArgumentException;
import org.almagestauth.exception.r406.AccessDeniedException;
import org.almagestauth.security.authentication.CustomUserDetails;
import org.almagestauth.utils.GenerateCodeUtil;
import org.almagestauth.utils.MailHandler;
import org.almagestauth.utils.encoder.Bcrypt;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender mailSender;
    private final JPAQueryFactory jpaQueryFactory;

    private static final String ALLOWED_SPECIAL_CHARACTERS = "!@_";
    private static final int PASSWORD_LENGTH = 8;


    /**
     * userDetailsService용 객체->userDetails변환. isbanned로직 추가
     */
    public CustomUserDetails findMemberToCustom(String membeId) {
        Optional<Member> findMember = memberRepository.findById(membeId);

        if (findMember.isEmpty()) {
            throw new IllegalArgumentException("해당 계정을 찾을 수 없습니다.");
        }

        Member member = findMember.get();

        // 계정이 금지된 경우 예외 처리
        if (member.getIsBanned().equals("T")) {
            throw new AccessDeniedException("해당 계정은 접속이 차단되었습니다.");
        }

        // 정상적인 경우 UserDetails로 변환하여 반환
        return member.toCustomUserDetails();
    }

    /**
     * 홈화면 사용자 정보 호출
     */
    public InfoResponseDto home(Member member) {
        try {
            return InfoResponseDto.builder()
                    .name(member.getName())
                    .isEnabled(member.getIsEnabled())
                    .email(member.getEmail())
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("사용자 조회 실패.");
        }
    }

    /**
     * 유저사용승인
     */
    public void enableUser(Member member) {
        try {
            member.enableUser();
            memberRepository.save(member);
        } catch (Exception e) {
            throw new IllegalArgumentException("사용자 승인 중 오류가 발생했습니다.");
        }
    }

    /**
     * 회원가입
     */
    public void register(RegisterRequestDto registerRequestDto) {
        if (registerRequestDto == null) {
            throw new IllegalArgumentException("회원가입 요청 정보 누락.");
        }
        if (registerRequestDto.getAccount() == null || registerRequestDto.getAccount().trim().isEmpty()) {
            throw new IllegalArgumentException("계정 정보가 유효하지 않습니다.");
        }

        if (registerRequestDto.getPassword() == null || registerRequestDto.getPassword().length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자리 이상이어야 합니다.");
        }

        if (registerRequestDto.getName() == null || registerRequestDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름이 유효하지 않습니다.");
        }

        if (registerRequestDto.getEmail() == null || !registerRequestDto.getEmail().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }

        Role defaultRole = getDefaultRole(1L);

        try {
            Member newMember = Member.builder()
                    .account(registerRequestDto.getAccount().trim())
                    .password(Bcrypt.encode(registerRequestDto.getPassword()))
                    .name(registerRequestDto.getName().trim())
                    .email(registerRequestDto.getEmail().trim())
                    .tel(Optional.ofNullable(registerRequestDto.getTel()).map(String::trim).orElse(null)) // 전화번호
                    .birthDate(Optional.ofNullable(registerRequestDto.getBirthDate()).map(String::trim).orElse(null)) // 생년월일
                    .gender(Optional.ofNullable(registerRequestDto.getGender()).map(String::trim).orElse(null)) // 성별
                    .country(Optional.ofNullable(registerRequestDto.getCountry()).map(String::trim).orElse(null)) // 국가
                    .role(defaultRole)
                    .isEnabled("F")
                    .isBanned("F")
                    .build();

            memberRepository.save(newMember);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("회원가입 중 오류가 발생했습니다.");
        }
    }


    /**
     * 비밀번호 변경. 계정 활성화.
     */
    public void changePassword(DataChangeRequestDto requestDto, Member member) {
        try {
            if (requestDto == null ||
                    !StringUtils.hasText(requestDto.getCurrentPassword()) ||
                    !StringUtils.hasText(requestDto.getNewPassword1()) ||
                    !StringUtils.hasText(requestDto.getNewPassword2())) {
                throw new IllegalArgumentException("비밀번호 정보가 누락되었습니다.");
            }

            if (!Bcrypt.matches(requestDto.getCurrentPassword(), member.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            if (!requestDto.getNewPassword1().equals(requestDto.getNewPassword2())) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }

            validatePassword(requestDto.getNewPassword1(), PASSWORD_LENGTH, null, null);

            member.changePassword(Bcrypt.encode(requestDto.getNewPassword1()));
            member.enableUser();
            memberRepository.save(member);


        } catch (Exception e) {
            log.error("비밀번호 변경 실패: {}", e.getMessage());
            throw new IllegalArgumentException("비밀번호 변경 실패");
        }
    }


    /**
     * 이메일 변경
     */
    public void changeEmail(String newEmail, Member member) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일 정보가 누락되었습니다.");
        }
        member.changeEmail(newEmail);
        try {
            memberRepository.save(member);
        } catch (Exception e) {
            throw new IllegalArgumentException("이메일 변경 실패");
        }
    }

    /**
     * 사용자 추가 정보 변경
     */
    public InfoResponseDto changeInfo(DataChangeRequestDto requestDto, Member member) {

        Optional<Member> byId = memberRepository.findById(member.getId());
        if(byId.isEmpty()){
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다");
        }

        Member saved = byId.get();


        saved.setName(requestDto.getName());
        saved.setCountry(requestDto.getCountry());
        saved.setGender(requestDto.getGender());
        saved.setBirthDate(requestDto.getBirthDate());
        saved.setLastUpdate(LocalDateTime.now());

        Member changed = memberRepository.save(saved);

        return InfoResponseDto.builder()
                .memberId(changed.getId())
                .isEnabled(changed.getIsEnabled())
                .name(changed.getName())
                .email(changed.getEmail())
                .gender(changed.getGender())
                .birthDate(changed.getBirthDate())
                .country(changed.getCountry())
                .lastUpdate(changed.getLastUpdate())
                .build();
    }

    /**
     * 계정 찾기
     */
    public InfoResponseDto findAccount(InfoRequestDto requestDto) {
        try {
            Optional<Member> findMember = memberRepository.findByEmail(requestDto.getEmail());

            if (findMember.isEmpty()) {
                throw new IllegalArgumentException("사용자조회 실패");
            }
            return InfoResponseDto.builder().account(findMember.get().getAccount()).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("계정을 찾을 수 없습니다.");
        }
    }

    /**
     * 비밀번호 초기화, 계정 비활성화 (현재 로그인 사용자)
     */
    public void initializePassword(Member member) {

        //임의비밀번호 발행시 새로운 패스워드 설정전까지 계정 비활성화
        String randomPassword = GenerateCodeUtil.generateRandomPassword();

        try {
            // 메일 발송 전에 계정 비활성화
            member.disableUser();

            //테스트계정의경우 임의생성번호를 12345678으로 고정
            if (member.getAccount().equals("tester12")) {
                randomPassword = "12345678";
            }

            MailHandler mailHandler = new MailHandler(mailSender);
            mailHandler.setTo(member.getEmail());
            mailHandler.setSubject("비밀번호 초기화");
            mailHandler.setText("초기화 비밀번호 : " + randomPassword, false);
            mailHandler.send();


            mailHandler.setText("초기화 비밀번호 : " + randomPassword, false);
            mailHandler.send();

            // 비밀번호 변경 및 저장
            member.changePassword(Bcrypt.encode(randomPassword));
            memberRepository.save(member);

        } catch (MailSendException e) {
            log.error("비밀번호 초기화 메일 발송 실패: {}", e.getMessage());
            throw new MailSendException("비밀번호 초기화 메일 발송에 실패했습니다", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("비밀번호 초기화 실패");
        }
    }

    /**
     * 유저정보 최신화 요청
     */
    public InfoResponseDto latestInfo(Member member) throws Exception {

        try {
            if (member.getIsEnabled().equals("T")) {
                LocalDateTime lastUpdate = member.getLastUpdate();

                LocalDateTime now = LocalDateTime.now();

                //마지막 수정일이 마지막 전송일보다 최근일경우 사용자정보전달
                if (lastUpdate == null || LocalDateTime.now().isAfter(lastUpdate)) {

                    member.updateDate(now);
                    memberRepository.save(member);

                    return InfoResponseDto.builder()
                            .memberId(member.getId())
                            .isEnabled(member.getIsEnabled())
                            .name(member.getName())
                            .email(member.getEmail())
                            .lastUpdate(member.getLastUpdate())
                            .build();
                } else {
                    throw new IllegalArgumentException("수정된 정보가 없습니다.");
                }
            } else {
                throw new IllegalArgumentException("미승인 사용자.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 소셜 회원가입용 유저정보
     */
    public InfoResponseDto socialRegister(Member member) throws Exception {
        try {
            if (member.getIsEnabled().equals("T")) {
                return InfoResponseDto.builder()
                        .memberId(member.getId())
                        .name(member.getName())
                        .isEnabled(member.getIsEnabled())
                        .email(member.getEmail())
                        .lastUpdate(member.getLastUpdate())
                        .build();
            } else {
                throw new IllegalAccessError("미승인 사용자.");
            }
        } catch (Exception e) {
            throw new IllegalAccessError("정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 비밀번호 초기화 (비인증 사용자)
     */
    public void resetPassword(DataChangeRequestDto requestDto) {
        if (requestDto == null ||
                !StringUtils.hasText(requestDto.getAccount()) ||
                !StringUtils.hasText(requestDto.getEmail())) {
            throw new IllegalArgumentException("계정 또는 이메일 정보가 누락되었습니다.");
        }
        Optional<Member> memberOptional = memberRepository.findByAccountAndEmail(
                requestDto.getAccount(),
                requestDto.getEmail()
        );
        if (memberOptional.isEmpty()) {
            throw new IllegalArgumentException("일치하는 사용자 정보를 찾을 수 없습니다.");
        }
        Member member = memberOptional.get();
        String randomPassword = GenerateCodeUtil.generateRandomPassword();

        try {
            // 계정 비활성화
            member.disableUser();

            // 이메일 발송
            MailHandler mailHandler = new MailHandler(mailSender);
            mailHandler.setTo(member.getEmail());
            mailHandler.setSubject("비밀번호 초기화");
            mailHandler.setText("초기화 비밀번호 : " + randomPassword, false);
            mailHandler.send();

            // 비밀번호 변경 및 저장
            member.changePassword(Bcrypt.encode(randomPassword));
            memberRepository.save(member);

        } catch (MailSendException e) {
            log.error("비밀번호 초기화 메일 발송 실패: {}", e.getMessage());
            throw new MailSendException("비밀번호 초기화 메일 발송에 실패했습니다", e);
        } catch (Exception e) {
            log.error("비밀번호 초기화 중 오류 발생: {}", e.getMessage());
            throw new IllegalArgumentException("비밀번호 초기화 처리 중 오류가 발생했습니다");
        }
    }

    /**
     * 계정 중복 조회
     */
    public void lookAccount(AuthRequestDto authRequestDto) {
        if (!StringUtils.hasText(authRequestDto.getAccount())) {
            throw new IllegalArgumentException("계정 정보가 누락되었습니다.");
        }

        QMember qMember = QMember.member;
        String result = jpaQueryFactory
                .select(qMember.account)
                .from(qMember)
                .where(qMember.account.eq(authRequestDto.getAccount()))
                .fetchOne();
        if (result != null) {
            throw new IllegalArgumentException("이미 사용중인 계정입니다.");
        }
    }

    /**
     * 메일 중복 조회
     */
    public void lookEmail(AuthRequestDto authRequestDto) {
        String email = authRequestDto.getEmail();
        if (!StringUtils.hasText(email) || !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }

        QMember qMember = QMember.member;
        String result = jpaQueryFactory
                .select(qMember.email)
                .from(qMember)
                .where(qMember.email.eq(email))
                .fetchOne();

        if (result != null) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
    }

    /**
     * 로그인시 전송된 사용자id와 로그인에 사용된 계정이 같은 사용자임을 검증 후 fcm토큰 저장. id값 리턴.
     */
    public void initFCM(AuthRequestDto request, Member member) {
        try {
            // null 체크 추가
            if (request == null || member == null) {
                throw new IllegalArgumentException("요청 정보가 누락되었습니다.");
            }

            if (request.getFirebaseToken() == null || request.getFirebaseToken().trim().isEmpty()) {
                throw new IllegalArgumentException("FCM 토큰이 누락되었습니다.");
            }

            // ID 검증 로직
            String requestId = request.getId();
            if (requestId == null || (!requestId.equals(member.getId()) && !requestId.equals("0"))) {
                throw new IllegalArgumentException("기기당 하나의 계정만 사용 가능합니다.");
            }

            member.updateFcmToken(request.getFirebaseToken().trim());
            memberRepository.save(member);

        } catch (Exception e) {
            log.error("FCM 토큰 초기화 중 오류 발생: {}", e.getMessage());
            throw new IllegalStateException("FCM 토큰 저장에 실패했습니다.", e);
        }
    }

    /**
     * 회원탈퇴기능. isBanned를 true로 변경 및 FCM 토큰 제거
     */
    public void leave(Member member) {
        try {
            if (member == null) {
                throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
            }

            QMember qMember = QMember.member;
            jpaQueryFactory.update(qMember)
                    .set(qMember.isEnabled, "N")
                    .set(qMember.isBanned, "T")
                    .set(qMember.firebaseToken, "")
                    .where(qMember.id.eq(member.getId()))
                    .execute();

        } catch (Exception e) {
            log.error("회원탈퇴 처리 중 오류 발생: {}", e.getMessage());
            throw new IllegalStateException("회원탈퇴 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * ban 여부를 체크하면서 객체를 리턴함
     */
    public Member checkBan(String account) {
        QMember qMember = QMember.member;
        Member member = jpaQueryFactory.selectFrom(qMember).where(
                qMember.account.eq(account)
                        .and(qMember.isBanned.eq("F"))
        ).fetchOne();

        if (member == null) {
            throw new AccessDeniedException("사용자 접속 차단됨");
        }

        return member;
    }

    /**
     * 길이 유효성 검사 (minLen 이상)
     */
    public static boolean isValidLength(String str, int minLen) {
        return str != null && str.length() >= minLen;
    }

    /**
     * 영문자, 숫자, 허용 특수문자 이외 문자 포함시 False
     */
    public static boolean containsAllowedCharacters(String str) {
        return str.matches("^[a-zA-Z0-9" + ALLOWED_SPECIAL_CHARACTERS + "]+$");
    }

    /**
     * 비밀번호 규칙 검사
     */
    public void validatePassword(String password, int minLen, String birth, String tel) {
        if (!isValidLength(password, minLen)) {
            throw new IllegalArgumentException("비밀번호는 최소 " + minLen + "자 이상이어야 합니다.");
        }
        if (!containsAllowedCharacters(password)) {
            throw new IllegalArgumentException("비밀번호는 허용되지 않은 문자를 포함할 수 없습니다.");
        }
        if (!containsRequiredTypes(password)) {
            throw new IllegalArgumentException("비밀번호에는 영문자, 숫자, 특수문자가 최소 1개 이상 포함되어야 합니다.");
        }
        if (hasRepeatedNumbers(password)) {
            throw new IllegalArgumentException("비밀번호에 동일한 숫자가 3번 이상 반복될 수 없습니다.");
        }
        if (hasSequentialNumbers(password)) {
            throw new IllegalArgumentException("비밀번호에 연속된 숫자가 포함될 수 없습니다.");
        }
    }


    /**
     * 기본 사용자 권한 조회
     */
    public Role getDefaultRole(Long id) {
        return roleRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("시스템 오류가 발생했습니다. 관리자에게 문의하세요."));
    }

    /**
     * 영문자, 숫자, 특수문자가 모두 1개 이상 포함되어있지 않으면 False
     */
    public static boolean containsRequiredTypes(String str) {
        return str.matches(".*[a-zA-Z].*") && // 영문자
                str.matches(".*[0-9].*") &&    // 숫자
                str.matches(".*[" + ALLOWED_SPECIAL_CHARACTERS + "].*"); // 특수문자
    }

    /**
     * 3개 이상의 동일한 숫자가 있다면 True
     */
    public static boolean hasRepeatedNumbers(String str) {
        return str.matches(".*(\\d)\\1{2,}.*");
    }

    /**
     * 연속 숫자 여부 확인
     * - 연속 숫자가 3개 이상인 경우, True
     * ex) 111, 123
     */
    public static boolean hasSequentialNumbers(String str) {
        for (int i = 0; i < str.length() - 2; i++) {
            char first = str.charAt(i);
            char second = str.charAt(i + 1);
            char third = str.charAt(i + 2);

            if (Character.isDigit(first) && Character.isDigit(second) && Character.isDigit(third)) {
                int diff1 = second - first;
                int diff2 = third - second;

                // 증가, 감소하는 연속 숫자 여부 확인
                if (diff1 == diff2 && Math.abs(diff1) == 1) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 권한 생성 for admin
     */
    public boolean insertRole(String role) {
        if (role == null) {
            return false;
        }
        Role newRole = new Role(role);
        try {
            roleRepository.save(newRole);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 권한 삭제 for admin
     */
    public boolean deleteRole(Long id) {
        if (id == null) {
            return false;
        }
        try {
            roleRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 신규 사용자 생성
     * */
    public void newMember(AuthRequestDto authRequestDto) {

    }



}
