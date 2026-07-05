package com.altees.portfolio.certification.repository;

import com.altees.portfolio.certification.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
}
