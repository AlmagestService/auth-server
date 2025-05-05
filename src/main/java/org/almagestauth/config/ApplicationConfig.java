package org.almagestauth.config;

import lombok.RequiredArgsConstructor;
import org.almagestauth.utils.encoder.Bcrypt;
import org.almagestauth.domain.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

  /**
   * 사용자 검증 로직
   * */

  private final MemberRepository memberRepository;
  private final Bcrypt bcrypt;

  @Bean
  public UserDetailsService userDetailsService() {
    return account -> memberRepository.findByAccount(account)
        .orElseThrow(() -> new UsernameNotFoundException("사용자조회 실패")).toCustomUserDetails();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(bcrypt);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }



}
