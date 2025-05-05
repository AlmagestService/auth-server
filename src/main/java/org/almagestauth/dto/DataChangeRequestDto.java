package org.almagestauth.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataChangeRequestDto {

    private String account;
    private String email;
    private String address;
    private String title;
    private String message;
    private String currentPassword;
    private String newPassword1;
    private String newPassword2;
    private String code;
    private String newEmail;
    private String tel;
    private String gender;
    private String birthDate;
    private String country;
    private String name;

}
