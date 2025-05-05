package org.almagestauth.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.almagestauth.domain.entity.Member;
import org.almagestauth.domain.repository.MemberRepository;
import org.almagestauth.dto.OtpTokenDto;
import org.almagestauth.exception.r400.IllegalArgumentException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMNotificationService {
    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;
    final String OTP_TITLE = "Almagest OTP";


    public void sendNotificationByToken(OtpTokenDto otpTokenDto) {
        if(otpTokenDto==null){
            throw new IllegalArgumentException("요청 데이터 누락.");
        }

        Optional<Member> savedMember = memberRepository.findById(otpTokenDto.getId());

        if (savedMember.isEmpty()) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        Member member = savedMember.get();

        // Firebase 토큰 확인
        if (member.getFirebaseToken() == null) {
            log.error("Firebase Token이 없습니다: " + otpTokenDto.getId());
            throw new IllegalArgumentException("앱 정보가 없습니다. 모바일 앱에서 최초 로그인 후 진행하세요.");
        }

        // 서버 응답 완료 후 0.3초 뒤에 알림 전송
        CompletableFuture.runAsync(() -> sendDelayedNotification(member, otpTokenDto));
    }

    private void sendDelayedNotification(Member member, OtpTokenDto otpTokenDto) {
        try {
            // 1초 딜레이
            Thread.sleep(300);

            Notification notification = Notification.builder()
                    .setTitle("OTP Code")
                    .build();

            Message message = Message.builder()
                    .setToken(member.getFirebaseToken())
                    .setNotification(notification)
                    .putData("type", "otp")
                    .putData("code", otpTokenDto.getCode())
                    .build();

            firebaseMessaging.send(message);
            log.info("알림 전송 성공: " + otpTokenDto.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("딜레이 중 스레드가 중단되었습니다.");
        } catch (Exception e) {
            log.error("알림 전송 중 오류 발생: " + e.getMessage());
        }
    }


    //firebase토큰 저장
    public String saveToken(OtpTokenDto requesDto) {
        try {
            Optional<Member> optionalMember = memberRepository.findById(requesDto.getId());

            if(optionalMember.isEmpty()){
                throw new IllegalArgumentException("잘못된 요청.");
            }

            Member member = optionalMember.get();

            member.updateFcmToken(requesDto.getToken());
            memberRepository.save(member);

            return "Firebase 토큰저장 성공";
        }catch (Exception e){
            throw new IllegalArgumentException("Firebase 토큰저장 실패");
        }
    }
}
