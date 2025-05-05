package org.almagestauth.controller.v1;

import lombok.RequiredArgsConstructor;
import org.almagestauth.common.dto.CommonResponseDto;
import org.almagestauth.dto.OtpTokenDto;
import org.almagestauth.service.FCMNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/a1/v1/fcm")
public class FCMNotificationControllerV1 {

    private final FCMNotificationService fcmNotificationService;

    /**
     * 알림 전송
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendNotificationByToken(@RequestBody OtpTokenDto requestDto){
        fcmNotificationService.sendNotificationByToken(requestDto);
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message("OTP 전송 성공")
                .repCode("SUCCESS")
                .repMsg("OTP 알림이 성공적으로 전송되었습니다")
                .build());
    }

    /**
     * 토큰 초기화
     */
    @PostMapping("/token")
    public ResponseEntity<?> insertFirebaseToken(@RequestBody OtpTokenDto requestDto){
        return ResponseEntity.ok(CommonResponseDto.builder()
                .status("200")
                .message(fcmNotificationService.saveToken(requestDto))
                .repCode("SUCCESS")
                .repMsg("Firebase 토큰이 성공적으로 저장되었습니다")
                .build());
    }
}
