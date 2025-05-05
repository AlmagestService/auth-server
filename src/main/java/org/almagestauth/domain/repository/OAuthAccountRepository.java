package org.almagestauth.domain.repository;

import org.almagestauth.domain.entity.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {
}
