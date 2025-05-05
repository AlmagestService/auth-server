package org.almagestauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "계정은 필수 값입니다.")
    @Size(min = 8, max = 20, message = "계정은 8자 이상, 20자 이하여야 합니다.")
    private String account;

    @NotBlank(message = "비밀번호는 필수 값입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야,  합니다.")
    private String password;

    @NotBlank(message = "이름은 필수 값입니다.")
    @Size(min = 2, max = 100, message = "이름은 2~20자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수 값입니다.")
    @Email(message = "올바른 이메일 형식으로 입력해주세요.")
    @Size(max = 50)
    private String email;


    @Size(max = 30, message = "전화번호는 최대 30자 입니다.")
    private String tel;

    @Size(min = 8, max = 8, message = "생년월일은 8자 입니다.")
    private String birthDate;

    @Size(max = 10, message = "성별은 최대 10자 입니다.")
    private String gender;

    @Size(max = 50, message = "국가는 최대 50자 입니다.")
    private String country;
}
