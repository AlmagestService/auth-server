package org.almagestauth.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OAuthAccount extends BaseTime{
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String provider;
    private String providerAccount_id;
    private LocalDateTime linkedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    @Comment("사용자 ID")
    private Member member;
}
