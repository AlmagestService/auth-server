package org.almagestauth.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.almagestauth.domain.entity.Member;
import org.almagestauth.domain.entity.MobileAppVersion;
import org.almagestauth.dto.*;
import org.almagestauth.security.authentication.CustomUserDetails;
import org.almagestauth.security.authentication.JwtProvider;
import org.almagestauth.service.*;
import org.almagestauth.utils.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthControllerV1.class)
class AuthControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private FCMNotificationService fcmNotificationService;

    @MockBean
    private RedisService redisService;

    @MockBean
    private AppVersionService appVersionService;

    private AuthRequestDto authRequestDto;
    private RegisterRequestDto registerRequestDto;
    private Member member;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 초기화
        authRequestDto = new AuthRequestDto();
        authRequestDto.setAccount("testuser");
        authRequestDto.setPassword("password123");

        registerRequestDto = new RegisterRequestDto();
        registerRequestDto.setAccount("testuser");
        registerRequestDto.setPassword("password123");
        registerRequestDto.setEmail("test@example.com");
        registerRequestDto.setName("Test User");

        member = Member.builder()
                .id("1")
                .account("testuser")
                .email("test@example.com")
                .name("Test User")
                .build();
    }

    @Test
    @DisplayName("계정 중복 확인 - 사용 가능한 계정")
    void lookAccount_Success() throws Exception {
        // given
        when(memberService.lookAccount(any(AuthRequestDto.class))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/auth/a1/v1/look/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("사용 가능한 계정입니다"));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 사용 가능한 이메일")
    void lookEmail_Success() throws Exception {
        // given
        when(memberService.lookEmail(any(AuthRequestDto.class))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/auth/a1/v1/look/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다"));
    }

    @Test
    @DisplayName("회원 가입 - 성공")
    void register_Success() throws Exception {
        // given
        when(memberService.register(any(RegisterRequestDto.class))).thenReturn(member);

        // when & then
        mockMvc.perform(post("/api/auth/a1/v1/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("201"))
                .andExpect(jsonPath("$.message").value("가입 성공"));
    }

    @Test
    @DisplayName("웹 로그인 - OTP 발급")
    void webAuthenticate_Success() throws Exception {
        // given
        when(memberService.checkBan(any(String.class))).thenReturn(member);
        when(otpService.auth(any(AuthRequestDto.class), any(Member.class)))
                .thenReturn(new OtpTokenDto());

        // when & then
        mockMvc.perform(post("/api/auth/a1/v1/login/web")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("OTP 코드가 전송되었습니다."));
    }

    @Test
    @DisplayName("모바일 로그인 - OTP 발급")
    void mobileAuthenticate_Success() throws Exception {
        // given
        when(memberService.checkBan(any(String.class))).thenReturn(member);
        when(otpService.auth(any(AuthRequestDto.class), any(Member.class)))
                .thenReturn(new OtpTokenDto());

        // when & then
        mockMvc.perform(post("/api/auth/a1/v1/login/app")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("OTP 코드가 전송되었습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("토큰 발급 - 성공")
    void publishToken_Success() throws Exception {
        // given
        when(otpService.check(any(AuthRequestDto.class), any(Member.class))).thenReturn(true);
        when(otpService.extractUserFromOtp(any(AuthRequestDto.class))).thenReturn(member);
        when(jwtProvider.generateAccessToken(any(CustomUserDetails.class))).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("refresh-token");

        // when & then
        mockMvc.perform(post("/api/auth/a1/v1/token/web")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("로그인 성공!"));
    }

    @Test
    @DisplayName("앱 버전 확인")
    void appVersion_Success() throws Exception {
        // given
        MobileAppVersion appVersion = new MobileAppVersion();
        appVersion.setVersionCode("1.0.0");
        when(appVersionService.getAppversion()).thenReturn(appVersion);

        // when & then
        mockMvc.perform(get("/api/auth/a1/v1/app/version"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.message").value("버전 조회 성공"));
    }
} 