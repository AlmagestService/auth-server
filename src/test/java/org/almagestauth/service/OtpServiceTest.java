package org.almagestauth.service;

import org.almagestauth.domain.entity.Member;
import org.almagestauth.dto.AuthRequestDto;
import org.almagestauth.dto.OtpTokenDto;
import org.almagestauth.utils.GenerateCodeUtil;
import org.almagestauth.utils.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private GenerateCodeUtil generateCodeUtil;

    @InjectMocks
    private OtpService otpService;

    private AuthRequestDto authRequestDto;
    private Member member;
    private OtpTokenDto otpTokenDto;

    @BeforeEach
    void setUp() {
        authRequestDto = new AuthRequestDto();
        authRequestDto.setAccount("testuser");
        authRequestDto.setPassword("password123");
        authRequestDto.setOtpCode("123456");

        member = Member.builder()
                .id("1")
                .account("testuser")
                .email("test@example.com")
                .name("Test User")
                .build();

        otpTokenDto = new OtpTokenDto();
        otpTokenDto.setOtpCode("123456");
        otpTokenDto.setId("1");
    }

    @Test
    @DisplayName("OTP 인증 - 성공")
    void auth_Success() {
        // given
        when(generateCodeUtil.generateOtpCode()).thenReturn("123456");
        when(redisService.setOtpCode(anyString(), anyString(), anyLong())).thenReturn(true);

        // when
        OtpTokenDto result = otpService.auth(authRequestDto, member);

        // then
        assertNotNull(result);
        assertEquals("123456", result.getOtpCode());
        verify(redisService).setOtpCode(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("OTP 검증 - 성공")
    void check_Success() {
        // given
        when(redisService.getOtpCode(anyString())).thenReturn("123456");

        // when
        boolean result = otpService.check(authRequestDto, member);

        // then
        assertTrue(result);
        verify(redisService).getOtpCode(anyString());
    }

    @Test
    @DisplayName("OTP 검증 - 실패 (코드 불일치)")
    void check_Failure_CodeMismatch() {
        // given
        when(redisService.getOtpCode(anyString())).thenReturn("654321");

        // when
        boolean result = otpService.check(authRequestDto, member);

        // then
        assertFalse(result);
        verify(redisService).getOtpCode(anyString());
    }

    @Test
    @DisplayName("OTP 검증 - 실패 (코드 만료)")
    void check_Failure_CodeExpired() {
        // given
        when(redisService.getOtpCode(anyString())).thenReturn(null);

        // when
        boolean result = otpService.check(authRequestDto, member);

        // then
        assertFalse(result);
        verify(redisService).getOtpCode(anyString());
    }

    @Test
    @DisplayName("OTP에서 사용자 정보 추출 - 성공")
    void extractUserFromOtp_Success() {
        // given
        when(redisService.getOtpCode(anyString())).thenReturn("123456");

        // when
        Member result = otpService.extractUserFromOtp(authRequestDto);

        // then
        assertNotNull(result);
        verify(redisService).getOtpCode(anyString());
    }

    @Test
    @DisplayName("OTP에서 사용자 정보 추출 - 실패")
    void extractUserFromOtp_Failure() {
        // given
        when(redisService.getOtpCode(anyString())).thenReturn(null);

        // when & then
        assertThrows(RuntimeException.class, () -> {
            otpService.extractUserFromOtp(authRequestDto);
        });
        verify(redisService).getOtpCode(anyString());
    }
} 