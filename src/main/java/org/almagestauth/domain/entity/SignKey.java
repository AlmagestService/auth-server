package org.almagestauth.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.security.PrivateKey;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignKey {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 2048)
    @Comment("JWT 서명용 키")
    private String privateKey;

    @Comment("해당 서비스")
    private String service;

    public SignKey(String privateKey, String service) {
        this.privateKey = privateKey;
        this.service = service;
    }
}
