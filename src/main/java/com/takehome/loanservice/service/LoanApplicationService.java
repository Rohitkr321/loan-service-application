package com.takehome.loanservice.service;

import com.takehome.loanservice.domain.LoanApplicationCommand;
import com.takehome.loanservice.domain.LoanDecision;
import com.takehome.loanservice.repository.LoanApplicationAuditEntity;
import com.takehome.loanservice.repository.LoanApplicationAuditRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplicationService {

	private final LoanDecisionEngine loanDecisionEngine;
	private final LoanApplicationAuditRepository loanApplicationAuditRepository;

	public LoanApplicationService(
			LoanDecisionEngine loanDecisionEngine,
			LoanApplicationAuditRepository loanApplicationAuditRepository) {
		this.loanDecisionEngine = loanDecisionEngine;
		this.loanApplicationAuditRepository = loanApplicationAuditRepository;
	}

	@Transactional
	public LoanDecision createApplication(LoanApplicationCommand command) {
		UUID applicationId = UUID.randomUUID();
		LoanDecision decision = loanDecisionEngine.evaluate(applicationId, command);
		LoanApplicationAuditEntity auditEntity =
				LoanApplicationAuditEntity.from(command, decision, Instant.now());
		loanApplicationAuditRepository.save(auditEntity);
		return decision;
	}
}
