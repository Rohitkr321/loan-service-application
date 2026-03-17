package com.takehome.loanservice.api;

import com.takehome.loanservice.api.request.LoanApplicationRequest;
import com.takehome.loanservice.api.response.LoanApplicationResponse;
import com.takehome.loanservice.domain.LoanDecision;
import com.takehome.loanservice.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the loan application evaluation endpoint.
 */
@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

	private final LoanApplicationService loanApplicationService;

	public LoanApplicationController(LoanApplicationService loanApplicationService) {
		this.loanApplicationService = loanApplicationService;
	}

	@PostMapping
	public ResponseEntity<LoanApplicationResponse> createApplication(
			@Valid @RequestBody LoanApplicationRequest request) {
		LoanDecision loanDecision = loanApplicationService.createApplication(request.toDomain());
		return ResponseEntity.ok(LoanApplicationResponse.from(loanDecision));
	}
}
