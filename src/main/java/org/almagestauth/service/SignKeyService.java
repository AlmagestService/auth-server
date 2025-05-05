package org.almagestauth.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.almagestauth.domain.entity.QSignKey;
import org.almagestauth.domain.entity.SignKey;
import org.almagestauth.exception.r500.CodeGenerationException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignKeyService {
    private final JPAQueryFactory query;

    /**
     *  서비스이름으로 db에 저장된 privateKey를 불러옴 
     */
    public String getPrivateKey(String serviceName) {
        QSignKey qSignKey = QSignKey.signKey;
        SignKey signKey = query.selectFrom(qSignKey).where(qSignKey.service.eq(serviceName)).fetchOne();
        if(signKey != null){
            return signKey.getPrivateKey();
        }else{
            log.error("서명 키 로드 실패");
            throw new CodeGenerationException("서명 키 로드 실패");
        }
    }

    /**
     * privatekey 문자열로부터 복원
     */
    public PrivateKey toPrivateKey(String stringPrivateKey) {
        try {
            byte[] decoded = Base64.getDecoder().decode(stringPrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        }catch (Exception e){
            log.error("서명 키 변환 중 오류 발생.", e);
            throw new CodeGenerationException("토큰 검증 오류.");
        }
    }

    /**
     * publicKey 문자열로부터 복원
     */
    public Key toPublicKey(String publicKey) {
        try {
            byte[] decoded = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        }catch (Exception e){
            log.error("공개 키 변환 중 오류 발생.", e);
            throw new CodeGenerationException("토큰 검증 오류.");
        }
    }
}
