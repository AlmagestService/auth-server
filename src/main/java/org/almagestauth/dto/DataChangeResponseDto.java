package org.almagestauth.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataChangeResponseDto {

    private String account;

    private String email;
    private String address;
    private String title;
    private String message;

    @Size(min = 6, max = 100)
    private String currentPassword;

    @Size(min = 6, max = 100)
    private String newPassword1;

    @Size(min = 6, max = 100)
    private String newPassword2;

    @Size(min = 4, max = 4)
    private String code;

    @Size(min = 8,max = 50)
    private String newEmail;


}
