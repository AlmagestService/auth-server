//package org.almagestauth.security.handler;
//
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.almagestauth.domain.entity.Member;
//import org.almagestauth.domain.repository.MemberRepository;
//import org.almagestauth.exception.r401.InvalidTokenException;
//import org.almagestauth.security.authentication.CustomUserDetails;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
//
//    private final MemberRepository memberRepository;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException {
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        String email;
//
//        try {
//            if (attributes.containsKey("kakao_account")) {
//                Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
//
//                // "email" 키가 있는지 확인 후 가져오기
//                if (account.containsKey("email")) {
//                    email = (String) account.get("email");
//                } else {
//                    log.error("OAuth2 로그인 실패 - 카카오 이메일 정보 없음");
//                    throw new InvalidTokenException("카카오 이메일 정보가 없습니다.");
//                }
//            } else {
//                log.error("OAuth2 로그인 실패 - 카카오 이메일 정보 없음");
//                throw new InvalidTokenException("카카오 계정 정보가 없습니다.");
//            }
//
//
//
//            // CustomUserDetails로 변환
//            CustomUserDetails userDetails =
//                    new CustomUserDetails(member, List.of(new SimpleGrantedAuthority("ROLE_USER")));
//
//            try {
//                String redirectUri = member.getIsEnabled().equals("F") ?
//                        "https://localhost:8180/signup" :
//                        "https://localhost:8180";
//                response.sendRedirect(redirectUri);
//            } catch (Exception e) {
//                log.error("Redirect 실패 - memberId={}", member.getId(), e);
//                throw new InvalidTokenException("Redirect 실패");
//            }
//        } catch (InvalidTokenException e) {
//            log.error("OAuth2 인증 처리 실패 - {}", e.getMessage());
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증 처리 중 오류 발생");
//        } catch (Exception e) {
//            log.error("OAuth2 인증 처리 중 예외 발생", e);
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류 발생");
//        }
//    }
//}
