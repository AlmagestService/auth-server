package org.almagestauth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.almagestauth.common.dto.CommonResponseDto;

import java.io.IOException;

@Slf4j
public class ResponseWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Exception 응답 출력
     */
    public static <T> void writeExceptionResponse(HttpServletResponse response, int httpStatus, CommonResponseDto<T> responseDto) {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 에러메시지 JSON으로 변환
        String jsonResponse = null;
        try {
            jsonResponse = objectMapper.writeValueAsString(responseDto);
        } catch (IOException e) {
            // JSON 변환 실패
            String status = (responseDto.getStatus() != null) ? responseDto.getStatus() : "FAILURE";
            String message = (responseDto.getMessage() != null) ? responseDto.getMessage() : "요청이 실패했습니다.";
            jsonResponse = "{\"status\": \"" + status + "\", \"message\": \"" + message + "\"}";
        }
        // 응답 출력
        try {
            response.getWriter().write(jsonResponse);
        } catch (IOException ex) {
            log.info("ExceptionResponseWriter::writeExceptionResponse - IOException: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

