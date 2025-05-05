package org.almagestauth.dto;

import lombok.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class InfoResponseDto {
    private String memberId;
    private String account;
    private String name;
    private String email;
    private String tel;
    private String birthDate;
    private String gender;
    private String country;

    private String isEnabled;
    private LocalDateTime lastUpdate;
    private String appVersion;

    private String errCount;



    /**
     * 홈화면용 생성자
     * */
    public InfoResponseDto(String name, String enabled, String email) {
        this.name = name;
        this.isEnabled = enabled;
        this.email = email;
    }

    /**
     * 가입용 정보 제공 생성자
     * */
    public InfoResponseDto(String id, String enabled, String name, String email, LocalDateTime lastUpdate) {
        this.memberId = id;
        this.isEnabled = enabled;
        this.name = name;
        this.email = email;
        this.lastUpdate = lastUpdate;
    }

    /**
     * 계정찾기 응답 생성자
     * */
    public InfoResponseDto(String account) {
        this.account = account;
    }
}
