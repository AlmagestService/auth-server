package org.almagestauth.service;

import lombok.RequiredArgsConstructor;
import org.almagestauth.domain.entity.Member;
import org.almagestauth.domain.entity.Otp;
import org.almagestauth.domain.repository.MemberRepository;
import org.almagestauth.domain.repository.OtpRepository;
import org.almagestauth.dto.AuthRequestDto;
import org.almagestauth.dto.DataChangeRequestDto;
import org.almagestauth.dto.OtpTokenDto;
import org.almagestauth.exception.r400.IllegalArgumentException;
import org.almagestauth.utils.OtpGenerator;
import org.almagestauth.utils.RedisService;
import org.almagestauth.utils.encoder.Bcrypt;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final MemberRepository memberRepository;
    private final OtpGenerator otpGenerator;
    private final RedisService redisService;

    /**사용자의 로그인정보가 일치하면 renew()를 호출해 Otp정보를 생성/갱신
     * 로그인정보가 일치하지않으면 예외를 발생시킨다. 
     */
    public OtpTokenDto auth(AuthRequestDto requestDto, Member member) {

        if(requestDto == null || member == null){
            throw new IllegalArgumentException("인증 정보 누락");
        }

        if (Bcrypt.matches(requestDto.getPassword(), member.getPassword())) {
            return renewOtp(member);
        } else {
            // 인증 실패 카운트 추가 및 잠금처리
            redisService.authFailureCountHandler(member.getId());
            return null;
        }
    }

    /**
     *  이메일 인증 코드 전송
     */
    public String emailOtpGenerate(Member member) {
        //테스트 계정이 아니면 OTP 생성 (앱스토어 제출시 필요)
        if (!member.getAccount().equals("tester12")) {
            otpGenerator.generateOtp(member);
        }
        // 생성된 OTP 조회
        Optional<Otp> savedOtp = otpRepository.findById(member.getId());

        if(savedOtp.isEmpty()){
            throw new IllegalArgumentException("OTP 정보 없음");
        }
        return savedOtp.get().getCode();
    }

    /**
     * 이메일 인증 코드 확인
     */
    public void check(DataChangeRequestDto requestDto, Member member) {

        if(requestDto == null){
            throw new IllegalArgumentException("인증 정보 누락");
        }

        // OTP 조회
        Otp otp = otpRepository.findById(member.getId())
                .orElseThrow(() -> new IllegalArgumentException("OTP를 찾을 수 없습니다."));

        // 테스트 계정 처리 (테스트 계정은 OTP 코드만 확인)
        if ("tester12".equals(member.getAccount())) {
            if(!requestDto.getCode().equals(otp.getCode())){
                throw new IllegalArgumentException("OTP 불일치");
            }
            return;
        }

        // OTP 만료 여부 확인
        if (otp.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP가 만료되었습니다.");
        }

        // OTP 코드 확인
        if (!requestDto.getCode().equals(otp.getCode())) {
            throw new IllegalArgumentException("OTP 코드가 일치하지 않습니다.");
        }

        // OTP 사용 여부 확인
        if (otp.isUsed()) {
            throw new IllegalArgumentException("이미 사용된 OTP입니다.");
        }


        try {
            // OTP 사용 처리
            otp.setUsed(true);
            otpRepository.save(otp);
        }catch (Exception e){
            throw new IllegalArgumentException("인증정보 처리 중 오류가 발생했습니다");
        }
    }

    /**
     *  사용자 식별자를 받아서 Otp객체를 만들고 otp코드를 생성하고 리턴한다. 
     */
    private OtpTokenDto renewOtp(Member member) {
        Otp otp = otpGenerator.generateOtp(member);

        OtpTokenDto otpTokenDto = new OtpTokenDto();

        otpTokenDto.setId(otp.getMemberId());
        otpTokenDto.setCode(otp.getCode());

        return otpTokenDto;
    }

    /**
     *  otp로 토큰발급시 otp에 저장된 식별자로 사용자정보를 조회해 리턴한다. 
     */
    public Member extractUserFromOtp(AuthRequestDto authRequestDto) {
        Optional<Otp> userOtp = Optional.empty();

        // ID로 Otp 조회
        if (authRequestDto.getId() != null && !authRequestDto.getId().isEmpty()) {
            userOtp = otpRepository.findById(authRequestDto.getId());
            if (userOtp.isEmpty()) {
                throw new IllegalArgumentException("ID를 통한 OTP 조회 실패.");
            }
        }

        // Account로 Otp 조회
        else if (authRequestDto.getAccount() != null && !authRequestDto.getAccount().isEmpty()) {
            Optional<Member> byAccount = memberRepository.findByAccount(authRequestDto.getAccount());
            if (byAccount.isEmpty()) {
                throw new IllegalArgumentException("계정을 통한 사용자 조회 실패.");
            }

            Member member = byAccount.get();
            userOtp = otpRepository.findById(member.getId());
            if (userOtp.isEmpty()) {
                throw new IllegalArgumentException("계정을 통한 OTP 조회 실패.");
            }
        }

        // 필수 입력값 누락 시 예외 처리
        else {
            throw new IllegalArgumentException("ID 또는 계정을 입력해야 합니다.");
        }

        return userOtp.get().getMember();
    }


    /**
     *  Otp객체의 code가 db에 저장된 값과 일치하는지, 시간이 만료되었는지, 이미 사용되었는지 검증한다.
     */
    public void check(AuthRequestDto authRequestDto, Member member) {
        String memberId;
        if (member != null) {
            memberId = member.getId();
        } else if (authRequestDto.getId() != null && !authRequestDto.getId().equals("")) {
            memberId = authRequestDto.getId();
        } else if (authRequestDto.getAccount() != null && !authRequestDto.getAccount().equals("")) {
            Optional<Member> byAccount = memberRepository.findByAccount(authRequestDto.getAccount());
            if (byAccount.isEmpty()) {
                throw new IllegalArgumentException("사용자를 찾을 수 없습니다");
            }
            memberId = byAccount.get().getId(); // 조회된 ID 사용
        } else {
            throw new IllegalArgumentException("사용자 정보 누락");
        }

        // OTP 조회
        Optional<Otp> userOtp = otpRepository.findById(memberId);

        if (userOtp.isEmpty()) {
            throw new IllegalArgumentException("인증정보 조회 실패.");
        }

        Otp otp = userOtp.get();

        // OTP가 이미 사용되었는지 확인
        if (otp.isUsed()) {
            throw new IllegalArgumentException("이미 사용된 OTP.");
        }

        // OTP 코드 확인
        if (authRequestDto.getCode() == null || !authRequestDto.getCode().equals(otp.getCode())) {
            redisService.authFailureCountHandler(memberId);
            throw new IllegalArgumentException("OTP 코드가 일치하지 않습니다.");
        }

        // OTP 만료 여부 확인
        if (otp.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP 기간 만료.");
        }

        // OTP 사용 처리
        otp.setUsed(true);
        otpRepository.save(otp);
    }
}
