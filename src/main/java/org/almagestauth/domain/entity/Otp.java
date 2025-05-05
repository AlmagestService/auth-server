package org.almagestauth.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
public class Otp { //Otp객체.
    @Id
    private String memberId;
    private String code;
    private LocalDateTime createdTime;
    private LocalDateTime expireTime;
    private boolean used;

    @OneToOne(fetch = FetchType.LAZY) // `memberId` 기준으로 Member와 연관관계 설정
    @JoinColumn(name = "memberId", referencedColumnName = "memberId", insertable = false, updatable = false)
    private Member member;

}
