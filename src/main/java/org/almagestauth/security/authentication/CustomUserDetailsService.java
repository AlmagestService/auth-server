package org.almagestauth.security.authentication;


import org.almagestauth.domain.entity.Member;
import org.almagestauth.domain.repository.MemberRepository;
import org.almagestauth.exception.r400.IllegalArgumentException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Username: memberID
     */
    @Override
    public CustomUserDetails loadUserByUsername(String memberId) throws IllegalArgumentException {
        Optional<Member> savedMember = memberRepository.findById(memberId);

        if(savedMember.isEmpty()){
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        Member member = savedMember.get();

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new CustomUserDetails(member, authorities);
    }
}
