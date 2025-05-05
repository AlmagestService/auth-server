package org.almagestauth.security.authentication;

import org.almagestauth.domain.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;



public class CustomUserDetails implements UserDetails {
    private Member member;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Member member, Collection<? extends GrantedAuthority> authorities) {
        this.member = member;
        this.authorities = authorities;
    }

    @Override
    public boolean isEnabled() {
        return member.getIsEnabled() != null && !member.getIsEnabled().equals('Y');
    }

    public Member getMember() {
        return member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    /**
     * 계정 잠김 여부
     * true : 잠기지 않음
     * false : 잠김
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.member.getIsBanned() != null && !this.member.getIsBanned().equals('Y');
    }
}
