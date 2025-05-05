package org.almagestauth.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDto {
    private String id;

    private String account;

    private String email;

    private String password;

    private String firebaseToken;

    private String serviceName;

    private String code;

    public AuthRequestDto(String account, String password, String serviceName) {
        this.account = account;
        this.password = password;
        this.serviceName = serviceName;
    }
}
