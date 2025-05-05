package org.almagestauth.utils;

import lombok.RequiredArgsConstructor;
import org.almagestauth.domain.entity.Member;
import org.almagestauth.domain.entity.Otp;
import org.almagestauth.domain.repository.OtpRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OtpGenerator {
    private final OtpRepository otpRepository;

    public Otp generateOtp(Member member){
        try {
            Otp otp = new Otp();
            String code = GenerateCodeUtil.generateOtpCode();
            // 테스트 계정은 OTP 고정값
            if(member.getAccount().equals("tester12")){
                code = "0000";
            }
            LocalDateTime now = LocalDateTime.now();

            otp.setMemberId(member.getId());
            otp.setCode(code);
            otp.setCreatedTime(now);
            otp.setExpireTime(now.plusMinutes(10));
            otp.setUsed(false);

            return otpRepository.save(otp);
        }catch (Exception e){
            throw new IllegalArgumentException("OTP 생성 실패");
        }
    }
}
