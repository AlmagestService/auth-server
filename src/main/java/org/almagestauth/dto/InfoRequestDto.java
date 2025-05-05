package org.almagestauth.dto;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoRequestDto {
    @Size(min = 2, max = 100)
    private String name;
    @Size(min = 6, max = 20)
    private String account;
    @Size(min = 8,max = 50)
    private String email;
    private boolean enabled;
}
