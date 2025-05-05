package org.almagestauth.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 필드가 null이면 직렬화에서 제외
public class CommonResponseDto<T> {
    private String status; // HTTP 상태 코드
    private String message; // 응답 메시지

    private String apriTranId; // 요청 시점의 트랜잭션 ID
    private String apriTranDtm; // 요청 시점의 트랜잭션 시간

    private String repCode; // 응답 코드 ( 서비스 코드 )
    private String repMsg; // 응답 메시지 ( 서비스 메시지 )

    private T data; // 요청별 응답 데이터 객체

    public CommonResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
