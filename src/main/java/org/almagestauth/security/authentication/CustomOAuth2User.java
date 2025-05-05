//package org.almagestauth.security.authentication;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.almagestauth.domain.entity.Member;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Map;
//
//@Getter
//@RequiredArgsConstructor
//public class CustomOAuth2User implements OAuth2User, UserDetails {
//
//	private final Member member;
//	private final Map<String, Object> attributes;
//	private final Collection<? extends GrantedAuthority> authorities;
//
//	/**
//	 * OAuth2 로그인 시 기본 권한을 "ROLE_USER"로 설정
//	 */
//	public CustomOAuth2User(Member member, Map<String, Object> attributes) {
//		this.member = member;
//		this.attributes = attributes;
//		this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
//	}
//
//	@Override
//	public String getName() {
//		return String.valueOf(member.getId()); // OAuth2에서 사용자를 식별할 수 있도록 ID 반환
//	}
//
//	@Override
//	public Map<String, Object> getAttributes() {
//		return attributes; // OAuth2 사용자 정보 반환
//	}
//
//	@Override
//	public Collection<? extends GrantedAuthority> getAuthorities() {
//		return authorities; // 기본적으로 ROLE_USER 부여
//	}
//
//	@Override
//	public String getUsername() {
//		return member.getAuthEmail();
//	}
//
//	@Override
//	public String getPassword() {
//		return null;
//	}
//
//	@Override
//	public boolean isAccountNonExpired() {
//		return true;
//	}
//
//	@Override
//	public boolean isAccountNonLocked() {
//		return member.getIsBanned() == null || !member.getIsBanned().equals('T');
//	}
//
//	@Override
//	public boolean isCredentialsNonExpired() {
//		return true;
//	}
//
//	@Override
//	public boolean isEnabled() {
//		return member.getIsEnabled() != null && member.getIsEnabled().equals('T');
//	}
//}
