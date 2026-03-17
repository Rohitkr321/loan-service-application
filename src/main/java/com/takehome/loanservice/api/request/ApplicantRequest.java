package com.takehome.loanservice.api.request;

import com.takehome.loanservice.domain.ApplicantProfile;
import com.takehome.loanservice.domain.EmploymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record ApplicantRequest(
		@NotBlank(message = "name is required")
		String name,
		@Min(value = 21, message = "age must be between 21 and 60")
		@Max(value = 60, message = "age must be between 21 and 60")
		int age,
		@NotNull(message = "monthlyIncome is required")
		@DecimalMin(value = "0.01", message = "monthlyIncome must be greater than 0")
		BigDecimal monthlyIncome,
		@NotNull(message = "employmentType is required")
		EmploymentType employmentType,
		@Min(value = 300, message = "creditScore must be between 300 and 900")
		@Max(value = 900, message = "creditScore must be between 300 and 900")
		int creditScore) {

	public ApplicantProfile toDomain() {
		return new ApplicantProfile(
				name == null ? null : name.strip(),
				age,
				monthlyIncome == null ? null : monthlyIncome.setScale(2, RoundingMode.HALF_UP),
				employmentType,
				creditScore);
	}
}
