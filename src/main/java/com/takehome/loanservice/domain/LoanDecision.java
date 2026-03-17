package com.takehome.loanservice.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable decision model used across the service and API layers.
 */
public record LoanDecision(
		UUID applicationId,
		ApplicationStatus status,
		RiskBand riskBand,
		LoanOffer offer,
		List<RejectionReason> rejectionReasons) {

	public LoanDecision {
		Objects.requireNonNull(applicationId, "applicationId must not be null");
		Objects.requireNonNull(status, "status must not be null");
		rejectionReasons = rejectionReasons == null ? List.of() : List.copyOf(rejectionReasons);

		if (status == ApplicationStatus.APPROVED) {
			Objects.requireNonNull(riskBand, "riskBand must not be null for approved decisions");
			Objects.requireNonNull(offer, "offer must not be null for approved decisions");
			if (!rejectionReasons.isEmpty()) {
				throw new IllegalArgumentException("Approved decisions cannot include rejection reasons");
			}
		}

		if (status == ApplicationStatus.REJECTED) {
			if (riskBand != null) {
				throw new IllegalArgumentException("Rejected decisions must not expose a risk band");
			}
			if (offer != null) {
				throw new IllegalArgumentException("Rejected decisions must not expose an offer");
			}
			if (rejectionReasons.isEmpty()) {
				throw new IllegalArgumentException("Rejected decisions must contain at least one reason");
			}
		}
	}

	public static LoanDecision approved(UUID applicationId, RiskBand riskBand, LoanOffer offer) {
		return new LoanDecision(applicationId, ApplicationStatus.APPROVED, riskBand, offer, List.of());
	}

	public static LoanDecision rejected(UUID applicationId, List<RejectionReason> rejectionReasons) {
		return new LoanDecision(applicationId, ApplicationStatus.REJECTED, null, null, rejectionReasons);
	}
}
