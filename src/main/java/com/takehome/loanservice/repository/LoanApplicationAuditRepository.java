package com.takehome.loanservice.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationAuditRepository extends JpaRepository<LoanApplicationAuditEntity, UUID> {
}
