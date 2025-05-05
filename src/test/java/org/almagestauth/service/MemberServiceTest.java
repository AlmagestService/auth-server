package org.almagestauth.service;

import org.almagestauth.domain.entity.Member;
import org.almagestauth.dto.AuthRequestDto;
import org.almagestauth.dto.RegisterRequestDto;
import org.almagestauth.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private AuthRequestDto authRequestDto;
    private RegisterRequestDto registerRequestDto;
    private Member member;

    @BeforeEach
    void setUp() {
        authRequestDto = new AuthRequestDto();
        authRequestDto.setAccount("testuser");
        authRequestDto.setPassword("password123");
        authRequestDto.setEmail("test@example.com");

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
                .password("encodedPassword")
                .build();
    }

    @Test
    @DisplayName("계정 중복 확인 - 사용 가능한 계정")
    void lookAccount_Success() {
        // given
        when(memberRepository.findByAccount(any(String.class))).thenReturn(Optional.empty());

        // when
        boolean result = memberService.lookAccount(authRequestDto);

        // then
        assertTrue(result);
        verify(memberRepository).findByAccount(authRequestDto.getAccount());
    }

    @Test
    @DisplayName("계정 중복 확인 - 이미 존재하는 계정")
    void lookAccount_Failure() {
        // given
        when(memberRepository.findByAccount(any(String.class))).thenReturn(Optional.of(member));

        // when
        boolean result = memberService.lookAccount(authRequestDto);

        // then
        assertFalse(result);
        verify(memberRepository).findByAccount(authRequestDto.getAccount());
    }

    @Test
    @DisplayName("이메일 중복 확인 - 사용 가능한 이메일")
    void lookEmail_Success() {
        // given
        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        // when
        boolean result = memberService.lookEmail(authRequestDto);

        // then
        assertTrue(result);
        verify(memberRepository).findByEmail(authRequestDto.getEmail());
    }

    @Test
    @DisplayName("이메일 중복 확인 - 이미 존재하는 이메일")
    void lookEmail_Failure() {
        // given
        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.of(member));

        // when
        boolean result = memberService.lookEmail(authRequestDto);

        // then
        assertFalse(result);
        verify(memberRepository).findByEmail(authRequestDto.getEmail());
    }

    @Test
    @DisplayName("회원 가입 - 성공")
    void register_Success() {
        // given
        when(memberRepository.findByAccount(any(String.class))).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // when
        Member result = memberService.register(registerRequestDto);

        // then
        assertNotNull(result);
        assertEquals(member.getId(), result.getId());
        assertEquals(member.getAccount(), result.getAccount());
        assertEquals(member.getEmail(), result.getEmail());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 가입 - 이미 존재하는 계정")
    void register_Failure_DuplicateAccount() {
        // given
        when(memberRepository.findByAccount(any(String.class))).thenReturn(Optional.of(member));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            memberService.register(registerRequestDto);
        });
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 가입 - 이미 존재하는 이메일")
    void register_Failure_DuplicateEmail() {
        // given
        when(memberRepository.findByAccount(any(String.class))).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.of(member));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            memberService.register(registerRequestDto);
        });
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 조회 - 성공")
    void checkBan_Success() {
        // given
        when(memberRepository.findByAccount(any(String.class))).thenReturn(Optional.of(member));

        // when
        Member result = memberService.checkBan(authRequestDto.getAccount());

        // then
        assertNotNull(result);
        assertEquals(member.getId(), result.getId());
        assertEquals(member.getAccount(), result.getAccount());
        verify(memberRepository).findByAccount(authRequestDto.getAccount());
    }

    @Test
    @DisplayName("회원 조회 - 존재하지 않는 계정")
    void checkBan_NotFound() {
        // given
        when(memberRepository.findByAccount(any(String.class))).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            memberService.checkBan(authRequestDto.getAccount());
        });
        verify(memberRepository).findByAccount(authRequestDto.getAccount());
    }

    @Test
    @DisplayName("FCM 토큰 초기화 - 성공")
    void initFCM_Success() {
        // given
        when(memberRepository.findByAccount(any(String.class))).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // when
        memberService.initFCM(authRequestDto, member);

        // then
        verify(memberRepository).save(any(Member.class));
    }
} 