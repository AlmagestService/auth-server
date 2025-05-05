package org.almagestauth.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mobile_app_version")
public class MobileAppVersion extends BaseTime{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mobile_version_id")
    private Long id;


    @Comment("앱 버전 코드")
    private String versionCode;

    @Column(nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("현재 버전 여부")
    private String isCurrent;
}
