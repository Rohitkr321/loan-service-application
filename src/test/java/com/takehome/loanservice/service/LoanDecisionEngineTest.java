package com.takehome.loanservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.takehome.loanservice.domain.ApplicantProfile;
import com.takehome.loanservice.domain.ApplicationStatus;
import com.takehome.loanservice.domain.EmploymentType;
import com.takehome.loanservice.domain.LoanApplicationCommand;
import com.takehome.loanservice.domain.LoanPurpose;
import com.takehome.loanservice.domain.RejectionReason;
import com.takehome.loanservice.domain.RequestedLoan;
import com.takehome.loanservice.domain.RiskBand;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Focused business-rule tests for pricing and eligibility decisions.
 */
class LoanDecisionEngineTest {

	private final LoanDecisionEngine loanDecisionEngine =
			new LoanDecisionEngine(new EmiCalculator(), new RiskBandClassifier());

	@Test
	void shouldRejectWhenCreditScoreIsBelowThreshold() {
		LoanApplicationCommand command = new LoanApplicationCommand(
				new ApplicantProfile("Amit", 30, new BigDecimal("50000.00"), EmploymentType.SALARIED, 599),
				new RequestedLoan(new BigDecimal("300000.00"), 24, LoanPurpose.AUTO));

		var loanDecision = loanDecisionEngine.evaluate(UUID.randomUUID(), command);

		assertThat(loanDecision.status()).isEqualTo(ApplicationStatus.REJECTED);
		assertThat(loanDecision.riskBand()).isNull();
		assertThat(loanDecision.rejectionReasons()).containsExactly(RejectionReason.CREDIT_SCORE_BELOW_600);
	}

	@Test
	void shouldRejectWhenAgeAndTenureLimitIsExceeded() {
		LoanApplicationCommand command = new LoanApplicationCommand(
				new ApplicantProfile("Amit", 60, new BigDecimal("120000.00"), EmploymentType.SALARIED, 780),
				new RequestedLoan(new BigDecimal("500000.00"), 72, LoanPurpose.HOME));

		var loanDecision = loanDecisionEngine.evaluate(UUID.randomUUID(), command);

		assertThat(loanDecision.status()).isEqualTo(ApplicationStatus.REJECTED);
		assertThat(loanDecision.rejectionReasons()).containsExactly(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED);
	}

	@Test
	void shouldApproveWhenApplicantMeetsAllRules() {
		LoanApplicationCommand command = new LoanApplicationCommand(
				new ApplicantProfile("Amit", 30, new BigDecimal("50000.00"), EmploymentType.SALARIED, 720),
				new RequestedLoan(new BigDecimal("500000.00"), 36, LoanPurpose.PERSONAL));

		var decision = loanDecisionEngine.evaluate(UUID.randomUUID(), command);

		assertThat(decision.status()).isEqualTo(ApplicationStatus.APPROVED);
		assertThat(decision.riskBand()).isEqualTo(RiskBand.MEDIUM);
		assertThat(decision.offer().interestRate()).isEqualByComparingTo("13.50");
		assertThat(decision.offer().emi()).isEqualByComparingTo("16967.64");
		assertThat(decision.offer().totalPayable()).isEqualByComparingTo("610835.04");
	}

	@Test
	void shouldRejectWhenCalculatedOfferExceedsFiftyPercentIncomeLimit() {
		LoanApplicationCommand command = new LoanApplicationCommand(
				new ApplicantProfile("Amit", 30, new BigDecimal("33000.00"), EmploymentType.SALARIED, 720),
				new RequestedLoan(new BigDecimal("500000.00"), 36, LoanPurpose.PERSONAL));

		var decision = loanDecisionEngine.evaluate(UUID.randomUUID(), command);

		assertThat(decision.status()).isEqualTo(ApplicationStatus.REJECTED);
		assertThat(decision.rejectionReasons()).containsExactly(RejectionReason.EMI_EXCEEDS_50_PERCENT_OFFER_LIMIT);
	}

	@Test
	void shouldApplySelfEmployedPremiumToFinalOffer() {
		LoanApplicationCommand command = new LoanApplicationCommand(
				new ApplicantProfile("Amit", 30, new BigDecimal("60000.00"), EmploymentType.SELF_EMPLOYED, 720),
				new RequestedLoan(new BigDecimal("500000.00"), 36, LoanPurpose.PERSONAL));

		var decision = loanDecisionEngine.evaluate(UUID.randomUUID(), command);

		assertThat(decision.status()).isEqualTo(ApplicationStatus.APPROVED);
		assertThat(decision.riskBand()).isEqualTo(RiskBand.MEDIUM);
		assertThat(decision.offer().interestRate()).isEqualByComparingTo("14.50");
		assertThat(decision.offer().emi()).isEqualByComparingTo("17210.49");
		assertThat(decision.offer().totalPayable()).isEqualByComparingTo("619577.64");
	}

	@Test
	void shouldApplyLargeLoanPremiumWhenAmountExceedsThreshold() {
		LoanApplicationCommand command = new LoanApplicationCommand(
				new ApplicantProfile("Amit", 35, new BigDecimal("100000.00"), EmploymentType.SALARIED, 780),
				new RequestedLoan(new BigDecimal("1500000.00"), 120, LoanPurpose.HOME));

		var decision = loanDecisionEngine.evaluate(UUID.randomUUID(), command);

		assertThat(decision.status()).isEqualTo(ApplicationStatus.APPROVED);
		assertThat(decision.riskBand()).isEqualTo(RiskBand.LOW);
		assertThat(decision.offer().interestRate()).isEqualByComparingTo("12.50");
		assertThat(decision.offer().emi()).isEqualByComparingTo("21956.43");
		assertThat(decision.offer().totalPayable()).isEqualByComparingTo("2634771.60");
	}

	@Test
	void shouldCollectAllEligibilityRejectionReasons() {
		LoanApplicationCommand command = new LoanApplicationCommand(
				new ApplicantProfile("Amit", 60, new BigDecimal("10000.00"), EmploymentType.SALARIED, 550),
				new RequestedLoan(new BigDecimal("500000.00"), 72, LoanPurpose.HOME));

		var decision = loanDecisionEngine.evaluate(UUID.randomUUID(), command);

		assertThat(decision.status()).isEqualTo(ApplicationStatus.REJECTED);
		assertThat(decision.riskBand()).isNull();
		assertThat(decision.rejectionReasons()).containsExactly(
				RejectionReason.CREDIT_SCORE_BELOW_600,
				RejectionReason.AGE_TENURE_LIMIT_EXCEEDED,
				RejectionReason.EMI_EXCEEDS_60_PERCENT);
	}
}
