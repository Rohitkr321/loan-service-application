package com.takehome.loanservice.service;

import com.takehome.loanservice.domain.EmploymentType;
import com.takehome.loanservice.domain.LoanApplicationCommand;
import com.takehome.loanservice.domain.LoanDecision;
import com.takehome.loanservice.domain.LoanOffer;
import com.takehome.loanservice.domain.RejectionReason;
import com.takehome.loanservice.domain.RiskBand;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Applies validation-independent business rules to produce a single decision.
 */
@Service
public class LoanDecisionEngine {

	private static final BigDecimal BASE_INTEREST_RATE = new BigDecimal("12.00");
	private static final BigDecimal MEDIUM_RISK_PREMIUM = new BigDecimal("1.50");
	private static final BigDecimal HIGH_RISK_PREMIUM = new BigDecimal("3.00");
	private static final BigDecimal SELF_EMPLOYED_PREMIUM = new BigDecimal("1.00");
	private static final BigDecimal LARGE_LOAN_PREMIUM = new BigDecimal("0.50");
	private static final BigDecimal LARGE_LOAN_THRESHOLD = new BigDecimal("1000000.00");
	private static final BigDecimal ELIGIBILITY_EMI_LIMIT = new BigDecimal("0.60");
	private static final BigDecimal OFFER_EMI_LIMIT = new BigDecimal("0.50");
	private static final int MAX_ELIGIBLE_AGE_IN_MONTHS = 65 * 12;

	private final EmiCalculator emiCalculator;
	private final RiskBandClassifier riskBandClassifier;

	public LoanDecisionEngine(EmiCalculator emiCalculator, RiskBandClassifier riskBandClassifier) {
		this.emiCalculator = emiCalculator;
		this.riskBandClassifier = riskBandClassifier;
	}

	public LoanDecision evaluate(UUID applicationId, LoanApplicationCommand command) {
		List<RejectionReason> rejectionReasons = new ArrayList<>();

		BigDecimal eligibilityEmi = emiCalculator.calculate(
				command.loan().amount(),
				BASE_INTEREST_RATE,
				command.loan().tenureMonths());

		if (command.applicant().creditScore() < 600) {
			rejectionReasons.add(RejectionReason.CREDIT_SCORE_BELOW_600);
		}
		if (ageTenureLimitExceeded(command)) {
			rejectionReasons.add(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED);
		}
		if (exceedsIncomeLimit(eligibilityEmi, command.applicant().monthlyIncome(), ELIGIBILITY_EMI_LIMIT)) {
			rejectionReasons.add(RejectionReason.EMI_EXCEEDS_60_PERCENT);
		}

		if (!rejectionReasons.isEmpty()) {
			return LoanDecision.rejected(applicationId, rejectionReasons);
		}

		RiskBand riskBand = riskBandClassifier.classify(command.applicant().creditScore());
		BigDecimal interestRate = calculateFinalInterestRate(command, riskBand);
		BigDecimal finalOfferEmi = emiCalculator.calculate(
				command.loan().amount(),
				interestRate,
				command.loan().tenureMonths());

		if (exceedsIncomeLimit(finalOfferEmi, command.applicant().monthlyIncome(), OFFER_EMI_LIMIT)) {
			return LoanDecision.rejected(
					applicationId,
					List.of(RejectionReason.EMI_EXCEEDS_50_PERCENT_OFFER_LIMIT));
		}

		BigDecimal totalPayable = finalOfferEmi
				.multiply(BigDecimal.valueOf(command.loan().tenureMonths()))
				.setScale(2, RoundingMode.HALF_UP);

		LoanOffer offer = new LoanOffer(
				interestRate,
				command.loan().tenureMonths(),
				finalOfferEmi,
				totalPayable);

		return LoanDecision.approved(applicationId, riskBand, offer);
	}

	private boolean ageTenureLimitExceeded(LoanApplicationCommand command) {
		int ageInMonths = command.applicant().age() * 12;
		return ageInMonths + command.loan().tenureMonths() > MAX_ELIGIBLE_AGE_IN_MONTHS;
	}

	private boolean exceedsIncomeLimit(BigDecimal emi, BigDecimal monthlyIncome, BigDecimal threshold) {
		BigDecimal limit = monthlyIncome.multiply(threshold).setScale(2, RoundingMode.HALF_UP);
		return emi.compareTo(limit) > 0;
	}

	private BigDecimal calculateFinalInterestRate(LoanApplicationCommand command, RiskBand riskBand) {
		return BASE_INTEREST_RATE
				.add(riskPremium(riskBand))
				.add(employmentPremium(command))
				.add(loanSizePremium(command))
				.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal riskPremium(RiskBand riskBand) {
		return switch (riskBand) {
			case LOW -> BigDecimal.ZERO;
			case MEDIUM -> MEDIUM_RISK_PREMIUM;
			case HIGH -> HIGH_RISK_PREMIUM;
		};
	}

	private BigDecimal employmentPremium(LoanApplicationCommand command) {
		return command.applicant().employmentType() == EmploymentType.SELF_EMPLOYED
				? SELF_EMPLOYED_PREMIUM
				: BigDecimal.ZERO;
	}

	private BigDecimal loanSizePremium(LoanApplicationCommand command) {
		return command.loan().amount().compareTo(LARGE_LOAN_THRESHOLD) > 0
				? LARGE_LOAN_PREMIUM
				: BigDecimal.ZERO;
	}
}
