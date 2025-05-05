package org.almagestauth.exception;

import lombok.extern.slf4j.Slf4j;
import org.almagestauth.common.dto.CommonResponseDto;
import org.almagestauth.dto.InfoResponseDto;
import org.almagestauth.exception.r400.AuthFailureException;
import org.almagestauth.exception.r400.IllegalArgumentException;
import org.almagestauth.exception.r401.InvalidTokenException;
import org.almagestauth.exception.r406.AccessDeniedException;
import org.almagestauth.exception.r500.CodeGenerationException;
import org.almagestauth.exception.r500.RedisSessionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailSendException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 BAD_REQUEST 통합
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("400")
                .message(ex.getMessage())
                .repCode("FAILURE")
                .repMsg("잘못된 요청입니다")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 BAD_REQUEST
    @ExceptionHandler(AuthFailureException.class)
    public ResponseEntity<?> handAuthFailureException(AuthFailureException ex) {
        log.warn("AuthFailureException - " + ex.getMessage());

        InfoResponseDto authResponseDto = new InfoResponseDto();
        authResponseDto.setErrCount(ex.getCount());

        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("400")
                .message(ex.getMessage())
                .repCode("FAILURE")
                .repMsg("인증에 실패했습니다")
                .data(authResponseDto)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CodeGenerationException.class)
    public ResponseEntity<?> handleCodeGenerationException(CodeGenerationException ex) {
        log.warn("CodeGenerationException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("500")
                .message(ex.getMessage())
                .repCode("FAILURE")
                .repMsg("코드 생성 중 오류가 발생했습니다")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<?> handleMailSendException(MailSendException ex) {
        log.error("메일 발송 오류: ", ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("500")
                .message(ex.getMessage())
                .repCode("FAILURE")
                .repMsg("메일 발송 중 오류가 발생했습니다")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("AccessDeniedException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("406")
                .message(ex.getMessage())
                .repCode("FAILURE")
                .repMsg("접근이 거부되었습니다")
                .build();
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
    }

    // FirebaseMessagingException 처리
    @ExceptionHandler(FirebaseMessagingException.class)
    public ResponseEntity<?> handleFirebaseMessagingException(FirebaseMessagingException ex) {
        log.error("FirebaseMessagingException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("500")
                .message(ex.getMessage())
                .repCode("FAILURE")
                .repMsg("FCM 메시지 전송 중 오류가 발생했습니다")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 400 BAD_REQUEST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException - " + ex.getMessage());
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        if (errorMessages.size() == 1) {
            CommonResponseDto<?> response = CommonResponseDto.builder()
                    .status("400")
                    .message(errorMessages.get(0))
                    .repCode("FAILURE")
                    .repMsg("입력값 검증에 실패했습니다")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "400");
            response.put("message", "입력값 검증에 실패했습니다");
            response.put("repCode", "FAILURE");
            response.put("repMsg", "다음 필드들이 잘못되었습니다: " + String.join(", ", errorMessages));
            response.put("errors", errorMessages);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 400 BAD_REQUEST
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("HttpMessageNotReadableException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("400")
                .message("요청 본문이 비어있거나 올바르지 않습니다.")
                .repCode("FAILURE")
                .repMsg("잘못된 요청 형식입니다")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 BAD_REQUEST
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("DataIntegrityViolationException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("400")
                .message("필수 데이터가 누락되었거나 잘못된 데이터가 입력되었습니다.")
                .repCode("FAILURE")
                .repMsg("데이터 무결성 검증에 실패했습니다")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 BAD_REQUEST
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("NoResourceFoundException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("400")
                .message("잘못된 요청입니다.")
                .repCode("FAILURE")
                .repMsg("요청한 리소스를 찾을 수 없습니다")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 500 INTERNAL_SERVER_ERROR
    @ExceptionHandler(RedisSessionException.class)
    public ResponseEntity<?> handleRedisSessionException(RedisSessionException ex) {
        log.warn("RedisSessionException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("500")
                .message("상태 관리 중 오류 발생.")
                .repCode("FAILURE")
                .repMsg("세션 관리 중 오류가 발생했습니다")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 400 BAD_REQUEST
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("HttpRequestMethodNotSupportedException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("400")
                .message("지원하지 않는 요청입니다.")
                .repCode("FAILURE")
                .repMsg("허용되지 않은 HTTP 메소드입니다")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 401 UNAUTHORIZED
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("InvalidTokenException - " + ex.getMessage());
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("401")
                .message(ex.getMessage())
                .repCode("FAILURE")
                .repMsg("유효하지 않은 토큰입니다")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 포괄적인 서버 오류 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleServerError(Exception ex) {
        log.warn("Exception - " + ex.getMessage());
        ex.printStackTrace();
        CommonResponseDto<?> response = CommonResponseDto.builder()
                .status("500")
                .message("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                .repCode("FAILURE")
                .repMsg("서버 내부 오류가 발생했습니다")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
