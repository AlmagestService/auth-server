package org.almagestauth.security.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService customUserDetailsService;
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {

                String account = (String) authentication.getPrincipal();
                String password = (String) authentication.getCredentials();

                CustomUserDetails member = customUserDetailsService.loadUserByUsername(account);

                if (!password.equals(member.getPassword())) {
                    throw new BadCredentialsException(account);
                }
                if (!member.isEnabled()) {
                    throw new BadCredentialsException(account);
                }

                return new UsernamePasswordAuthenticationToken(authentication, password, member.getAuthorities());
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return false;
            }
        }
