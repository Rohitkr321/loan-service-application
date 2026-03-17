package com.takehome.loanservice.domain;

import java.math.BigDecimal;

public record ApplicantProfile(
		String name,
		int age,
		BigDecimal monthlyIncome,
		EmploymentType employmentType,
		int creditScore) {
}
