package com.takehome.loanservice.repository;

import com.takehome.loanservice.domain.ApplicationStatus;
import com.takehome.loanservice.domain.EmploymentType;
import com.takehome.loanservice.domain.LoanApplicationCommand;
import com.takehome.loanservice.domain.LoanDecision;
import com.takehome.loanservice.domain.LoanOffer;
import com.takehome.loanservice.domain.LoanPurpose;
import com.takehome.loanservice.domain.RejectionReason;
import com.takehome.loanservice.domain.RiskBand;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persistence model for storing application decisions for audit purposes.
 */
@Entity
@Table(name = "loan_application_audit")
public class LoanApplicationAuditEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID applicationId;

	@Column(nullable = false)
	private String applicantName;

	@Column(nullable = false)
	private int applicantAge;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal monthlyIncome;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EmploymentType employmentType;

	@Column(nullable = false)
	private int creditScore;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal loanAmount;

	@Column(nullable = false)
	private int tenureMonths;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LoanPurpose loanPurpose;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ApplicationStatus status;

	@Enumerated(EnumType.STRING)
	private RiskBand riskBand;

	@Column(precision = 5, scale = 2)
	private BigDecimal interestRate;

	@Column(precision = 19, scale = 2)
	private BigDecimal emi;

	@Column(precision = 19, scale = 2)
	private BigDecimal totalPayable;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name = "loan_application_rejection_reason",
			joinColumns = @JoinColumn(name = "application_id"))
	@OrderColumn(name = "reason_order")
	@Enumerated(EnumType.STRING)
	@Column(name = "reason_code")
	private List<RejectionReason> rejectionReasons = new ArrayList<>();

	protected LoanApplicationAuditEntity() {
	}

	private LoanApplicationAuditEntity(
			UUID applicationId,
			String applicantName,
			int applicantAge,
			BigDecimal monthlyIncome,
			EmploymentType employmentType,
			int creditScore,
			BigDecimal loanAmount,
			int tenureMonths,
			LoanPurpose loanPurpose,
			ApplicationStatus status,
			RiskBand riskBand,
			BigDecimal interestRate,
			BigDecimal emi,
			BigDecimal totalPayable,
			Instant createdAt,
			List<RejectionReason> rejectionReasons) {
		this.applicationId = applicationId;
		this.applicantName = applicantName;
		this.applicantAge = applicantAge;
		this.monthlyIncome = monthlyIncome;
		this.employmentType = employmentType;
		this.creditScore = creditScore;
		this.loanAmount = loanAmount;
		this.tenureMonths = tenureMonths;
		this.loanPurpose = loanPurpose;
		this.status = status;
		this.riskBand = riskBand;
		this.interestRate = interestRate;
		this.emi = emi;
		this.totalPayable = totalPayable;
		this.createdAt = createdAt;
		this.rejectionReasons = new ArrayList<>(rejectionReasons);
	}

	public static LoanApplicationAuditEntity from(
			LoanApplicationCommand command, LoanDecision decision, Instant createdAt) {
		LoanOffer offer = decision.offer();
		return new LoanApplicationAuditEntity(
				decision.applicationId(),
				command.applicant().name(),
				command.applicant().age(),
				scale(command.applicant().monthlyIncome()),
				command.applicant().employmentType(),
				command.applicant().creditScore(),
				scale(command.loan().amount()),
				command.loan().tenureMonths(),
				command.loan().purpose(),
				decision.status(),
				decision.riskBand(),
				offer == null ? null : scale(offer.interestRate()),
				offer == null ? null : scale(offer.emi()),
				offer == null ? null : scale(offer.totalPayable()),
				createdAt,
				decision.rejectionReasons());
	}

	public UUID getApplicationId() {
		return applicationId;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	private static BigDecimal scale(BigDecimal value) {
		return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
	}
}
