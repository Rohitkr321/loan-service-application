package com.takehome.loanservice.domain;

public enum RejectionReason {
	CREDIT_SCORE_BELOW_600,
	AGE_TENURE_LIMIT_EXCEEDED,
	EMI_EXCEEDS_60_PERCENT,
	EMI_EXCEEDS_50_PERCENT_OFFER_LIMIT
}
