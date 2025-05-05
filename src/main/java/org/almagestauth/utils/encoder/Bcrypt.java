package org.almagestauth.utils.encoder;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 비밀번호 암호화
 * */
@Component
public class Bcrypt extends BCryptPasswordEncoder{
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encode(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public static boolean matches(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

}
