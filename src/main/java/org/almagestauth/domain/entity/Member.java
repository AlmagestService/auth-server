package org.almagestauth.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.almagestauth.security.authentication.CustomUserDetails;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Table(name = "member")
public class Member extends BaseTime implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //기본정보
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_id", columnDefinition = "VARCHAR(40)")
    @Comment("사용자 고유 ID")
    @Size(max = 40)
    private String id;

    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(20)")
    @Size(min = 8, max = 20)
    @Comment("사용자 계정")
    private String account;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    @Size(min = 8, max = 100)
    private String password;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    @Size(min = 2, max = 100)
    @Comment("사용자 이름")
    private String name;

    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(50)")
    @Size(min = 8,max = 50)
    @Comment("사용자 이메일")
    private String email;

    @Column(unique = true, columnDefinition = "VARCHAR(30)")
    @Size(max = 30)
    @Comment("사용자 전화번호")
    private String tel;

    @Column(columnDefinition = "VARCHAR(8)")
    @Size(min = 8, max = 8)
    @Comment("사용자 생년원일")
    private String birthDate;

    @Column(columnDefinition = "VARCHAR(10)")
    @Size(max = 10)
    @Comment("사용자 성별")
    private String gender;

    @Column(columnDefinition = "VARCHAR(50)")
    @Size(max = 50)
    @Comment("사용자 국가")
    private String country;

    @Column(nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("사용자 사용승인 여부")
    private String isEnabled;

    @Column(nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @JsonProperty("banned")
    @Comment("사용자 차단 여부")
    private String isBanned;

    @Column(name = "last_update", columnDefinition = "DATETIME")
    @Comment("마지막 정보 업데이트 날짜")
    private LocalDateTime lastUpdate;

    @Column(columnDefinition = "VARCHAR(255)")
    @Comment("OTP알림용 FCM토큰")
    private String firebaseToken;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    @Comment("사용자 권한")
    private Role role;

    @OneToMany(mappedBy = "member")
    private List<OAuthAccount> oAuthAccountList = new ArrayList<>();


    public Member(String account, String password, String name, String email, Role role, String isBanned) {
        this.account = account;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isBanned = isBanned;
    }


    /**
     * 마지막 업데이트 날짜 갱신
     */
    public void updateLastModifiedDate() {
        this.lastUpdate = LocalDateTime.now();
    }

    /**
     * 사용자 정보를 UserDetails로 변환
     */
    public CustomUserDetails toCustomUserDetails() {
        return new CustomUserDetails(
                this,
                Collections.singletonList(new SimpleGrantedAuthority(this.role.getName()))
        );
    }

    /**
     * 사용자 활성화
     */
    public void enableUser() {
        this.isEnabled = "T";
    }

    /**
     * 사용자 비활성화
     */
    public void disableUser() {
        this.isEnabled = "F";
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 100) {
            throw new IllegalArgumentException("비밀번호는 6자 이상 100자 이하로 설정해야 합니다.");
        }
        this.password = newPassword;
    }

    /**
     * 이메일 변경
     */
    public void changeEmail(String newEmail) {
        if (newEmail == null || !newEmail.matches("^[A-Za-z0-9+_.-]+@[a-zA-Z0-9.-]+$")) {
            throw new IllegalArgumentException("올바른 이메일 형식을 입력하세요.");
        }
        this.email = newEmail;
    }

    /**
     * FCM 토큰 설정
     */
    public void updateFcmToken(String token) {
        this.firebaseToken = token;
    }

    public void changeRole(Role role){this.role = role;}

    /**
     * 정보 최신화 날짜 수정
     * */
    public void updateDate(LocalDateTime updateDate) {
        this.lastUpdate = updateDate;
    }

}
