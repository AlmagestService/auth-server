package org.almagestauth.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;


@Component
@RequiredArgsConstructor
public class RSAGenerator {

    // RSA 키 페어 생성
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // 키 크기 설정 (일반적으로 2048비트 사용)
        return keyPairGenerator.generateKeyPair();
    }

    public KeyPair createRSA() throws Exception {
        return generateKeyPair();
    }
}
