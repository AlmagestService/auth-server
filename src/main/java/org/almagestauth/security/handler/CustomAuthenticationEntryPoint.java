package org.almagestauth.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.almagestauth.common.dto.CommonResponseDto;
import org.almagestauth.exception.ResponseWriter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.info("CustomAuthenticationEntryPoint - Exception Message: " + authException.getMessage());
        ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_UNAUTHORIZED, new CommonResponseDto<>("UNAUTHORIZED", "로그인이 필요합니다."));
    }
}
