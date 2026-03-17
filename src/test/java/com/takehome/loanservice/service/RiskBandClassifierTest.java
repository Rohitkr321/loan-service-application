package com.takehome.loanservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.takehome.loanservice.domain.RiskBand;
import org.junit.jupiter.api.Test;

class RiskBandClassifierTest {

	private final RiskBandClassifier riskBandClassifier = new RiskBandClassifier();

	@Test
	void shouldClassifyLowRiskBandAtSevenHundredFiftyAndAbove() {
		assertThat(riskBandClassifier.classify(750)).isEqualTo(RiskBand.LOW);
	}

	@Test
	void shouldClassifyMediumRiskBandBetweenSixHundredFiftyAndSevenHundredFortyNine() {
		assertThat(riskBandClassifier.classify(749)).isEqualTo(RiskBand.MEDIUM);
	}

	@Test
	void shouldClassifyHighRiskBandBetweenSixHundredAndSixHundredFortyNine() {
		assertThat(riskBandClassifier.classify(649)).isEqualTo(RiskBand.HIGH);
	}
}
