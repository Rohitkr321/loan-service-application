package com.takehome.loanservice.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class EmiCalculator {

	private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);
	private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);
	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	public BigDecimal calculate(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
		BigDecimal monthlyRate = annualInterestRate
				.divide(HUNDRED, 10, RoundingMode.HALF_UP)
				.divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);

		BigDecimal growthFactor = BigDecimal.ONE.add(monthlyRate).pow(tenureMonths, MATH_CONTEXT);
		BigDecimal numerator = principal
				.multiply(monthlyRate, MATH_CONTEXT)
				.multiply(growthFactor, MATH_CONTEXT);
		BigDecimal denominator = growthFactor.subtract(BigDecimal.ONE, MATH_CONTEXT);

		return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
	}
}
