package org.almagestauth.config;

import lombok.RequiredArgsConstructor;
import org.almagestauth.security.authentication.CustomAuthenticationProvider;
import org.almagestauth.security.authentication.JwtFilter;
import org.almagestauth.security.handler.CustomAccessDeniedHandler;
import org.almagestauth.security.handler.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomAuthenticationProvider customAuthenticationProvider;

    private static final String[] PUBLIC_API_URL = {"/api/a1/**", "/v3/api-docs/**", "/swagger-ui/**" }; // 인증 없이도 접근 가능한 경로
    private static final String ADMIN_API_URL = "/admin/**"; // 관리자만 접근 가능한 경로

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .cors(cors -> cors.configurationSource(CorsConfig.corsConfigurationSource())) // CORS 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless 설정
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll()
//                        .requestMatchers(PUBLIC_API_URL).permitAll()
//                        .requestMatchers("/api/a2/**").hasAnyRole("USER", "ADMIN")
//                        .requestMatchers(ADMIN_API_URL).hasRole("ADMIN")
//                        .anyRequest().authenticated()
                )
                .exceptionHandling((e) -> e
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 인증되지 않은 사용자 접근 혹은 유효한 인증정보 부족한 경우(401 Unauthorized)
                        .accessDeniedHandler(new CustomAccessDeniedHandler()) // 403 Forbidden
                )
                .authenticationProvider(customAuthenticationProvider) // Custom Authentication Provider 설정
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // JwtFilter 추가
                .build();
    }
}
