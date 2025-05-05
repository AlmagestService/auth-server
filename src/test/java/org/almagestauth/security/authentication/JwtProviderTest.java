package org.almagestauth.security.authentication;

import org.almagestauth.domain.entity.Member;
import org.almagestauth.security.authentication.JwtProvider;
import org.almagestauth.utils.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private JwtProvider jwtProvider;

    private CustomUserDetails userDetails;
    private String accessToken;
    private String refreshToken;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // RSA 키 페어 생성
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();

        // 테스트용 사용자 정보 설정
        Member member = Member.builder()
                .id("1")
                .account("testuser")
                .email("test@example.com")
                .name("Test User")
                .build();
        userDetails = new CustomUserDetails(member);

        // JWT 설정 값 주입
        ReflectionTestUtils.setField(jwtProvider, "privateKey", Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        ReflectionTestUtils.setField(jwtProvider, "publicKey", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        ReflectionTestUtils.setField(jwtProvider, "accessExpiration", 600000L); // 10분
        ReflectionTestUtils.setField(jwtProvider, "refreshExpiration", 15552000000L); // 180일

        // 토큰 생성
        accessToken = jwtProvider.generateAccessToken(userDetails);
        refreshToken = jwtProvider.generateRefreshToken(userDetails);
    }

    @Test
    @DisplayName("Access 토큰 생성 및 검증 - 성공")
    void generateAndValidateAccessToken_Success() {
        // given
        String token = jwtProvider.generateAccessToken(userDetails);

        // when
        boolean isValid = jwtProvider.validateAccessToken(token, userDetails);

        // then
        assertTrue(isValid);
        assertEquals(userDetails.getMember().getId(), jwtProvider.extractMemberId(token));
    }

    @Test
    @DisplayName("Refresh 토큰 생성 및 검증 - 성공")
    void generateAndValidateRefreshToken_Success() {
        // given
        String token = jwtProvider.generateRefreshToken(userDetails);
        when(redisService.getRefreshTokenVerification(anyString())).thenReturn("verifyString");

        // when
        jwtProvider.validateRefreshToken(token);

        // then
        verify(redisService).getRefreshTokenVerification(anyString());
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 - 성공")
    void extractMemberId_Success() {
        // given
        String token = jwtProvider.generateAccessToken(userDetails);

        // when
        String memberId = jwtProvider.extractMemberId(token);

        // then
        assertEquals(userDetails.getMember().getId(), memberId);
    }

    @Test
    @DisplayName("토큰 만료 검증 - 만료되지 않은 토큰")
    void validateToken_NotExpired() {
        // given
        String token = jwtProvider.generateAccessToken(userDetails);

        // when
        boolean isValid = jwtProvider.validateAccessToken(token, userDetails);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("토큰 만료 검증 - 만료된 토큰")
    void validateToken_Expired() {
        // given
        ReflectionTestUtils.setField(jwtProvider, "accessExpiration", -1L);
        String expiredToken = jwtProvider.generateAccessToken(userDetails);

        // when & then
        assertThrows(Exception.class, () -> {
            jwtProvider.validateAccessToken(expiredToken, userDetails);
        });
    }

    @Test
    @DisplayName("토큰 발행자 검증 - 성공")
    void validateToken_Issuer() {
        // given
        String token = jwtProvider.generateAccessToken(userDetails);

        // when
        boolean isValid = jwtProvider.validateAccessToken(token, userDetails);

        // then
        assertTrue(isValid);
    }
} 