package org.almagestauth.domain.repository;

import org.almagestauth.domain.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, String> {

}
