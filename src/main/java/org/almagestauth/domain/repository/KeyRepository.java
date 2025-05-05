package org.almagestauth.domain.repository;

import org.almagestauth.domain.entity.SignKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyRepository extends JpaRepository<SignKey, Long> {
}
