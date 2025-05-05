package org.almagestauth.domain.repository;

import org.almagestauth.domain.entity.MobileAppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppVersionRepository extends JpaRepository<MobileAppVersion, Long> {
}
