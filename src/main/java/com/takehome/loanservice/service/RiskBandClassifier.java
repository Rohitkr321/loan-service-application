package com.takehome.loanservice.service;

import com.takehome.loanservice.domain.RiskBand;
import org.springframework.stereotype.Component;

@Component
public class RiskBandClassifier {

	public RiskBand classify(int creditScore) {
		if (creditScore >= 750) {
			return RiskBand.LOW;
		}
		if (creditScore >= 650) {
			return RiskBand.MEDIUM;
		}
		if (creditScore >= 600) {
			return RiskBand.HIGH;
		}
		throw new IllegalArgumentException("Risk band is only defined for credit scores of 600 and above");
	}
}
