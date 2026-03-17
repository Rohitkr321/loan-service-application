package com.takehome.loanservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class EmiCalculatorTest {

	private final EmiCalculator emiCalculator = new EmiCalculator();

	@Test
	void shouldCalculateEmiUsingBigDecimalPrecision() {
		BigDecimal emi = emiCalculator.calculate(new BigDecimal("500000.00"), new BigDecimal("12.00"), 36);

		assertThat(emi).isEqualByComparingTo("16607.15");
	}
}
