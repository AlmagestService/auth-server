package org.almagestauth.service;

import lombok.RequiredArgsConstructor;
import org.almagestauth.domain.entity.Member;
import org.almagestauth.utils.MailHandler;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final JavaMailSender javaMailSender;

    /**
     * 이메일 인증 코드 전송
     */
    public void send(String code, Member member) {
        String to = member.getEmail();
        sendEmail(code, to, "Almagest 이메일 인증");
    }
    /**
     * 새 이메일 인증 코드 전송
     */
    public void sendNew(String code, String newEmail) {
        sendEmail(code, newEmail, "Almagest 이메일 변경 인증");
    }

    /**
     * 이메일 전송
     */
    public void sendEmail(String code, String toEmail, String subject) {
        try {
            MailHandler mailHandler = new MailHandler(javaMailSender);

            mailHandler.setTo(toEmail); // 받는 사람 주소
            mailHandler.setSubject(subject); // 제목

            // 이메일 본문
            String htmlContent = "<p>" + "인증 코드 : " + code + "</p>";
            mailHandler.setText(htmlContent, true); // 내용, HTML 형식 설정

            mailHandler.send(); // 메일 전송
        } catch (Exception e) {
            throw new MailSendException("메일 전송 실패");
        }
    }


}
