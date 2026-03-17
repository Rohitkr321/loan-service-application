package com.takehome.loanservice.domain;

public record LoanApplicationCommand(
		ApplicantProfile applicant,
		RequestedLoan loan) {
}
