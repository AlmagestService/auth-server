package org.almagestauth.domain.repository;

import org.almagestauth.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByAccount(String account);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByAccountAndEmail(String account, String email);
}
