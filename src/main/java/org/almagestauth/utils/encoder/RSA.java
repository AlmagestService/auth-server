package org.almagestauth.utils.encoder;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSA {


    /*문자열을 RSA로 암호화*/
    public String encrypt(String plaintext, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /* RSA로 암호화된 문자열을 복호화*/
    public String decrypt(String encryptedText, PublicKey publicKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }





//    public String createRSA(String serviceName) {
//        try {
//            //rsa 키페어 생성
//            KeyPair keyPair = rsaGenerator.createRSA();
//
//            //키 문자열로 변환
//            String stringPublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
//            String stringPrivateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
//
//            SignKey signKey = new SignKey(
//                    stringPublicKey,
//                    serviceName
//            );
//
//            //DB에 publicKey 저장
//            keyRepository.save(signKey);
//
//            //privateKey 리턴
//            return stringPrivateKey;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
