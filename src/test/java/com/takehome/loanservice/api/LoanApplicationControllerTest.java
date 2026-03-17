package com.takehome.loanservice.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.takehome.loanservice.repository.LoanApplicationAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoanApplicationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LoanApplicationAuditRepository loanApplicationAuditRepository;

	@BeforeEach
	void setUp() {
		loanApplicationAuditRepository.deleteAll();
	}

	@Test
	void shouldApproveAnEligibleApplication() throws Exception {
		String payload = """
				{
				  "applicant": {
				    "name": "Rohit Kumar",
				    "age": 30,
				    "monthlyIncome": 50000,
				    "employmentType": "SALARIED",
				    "creditScore": 720
				  },
				  "loan": {
				    "amount": 500000,
				    "tenureMonths": 36,
				    "purpose": "PERSONAL"
				  }
				}
				""";

		mockMvc.perform(post("/applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.applicationId").isNotEmpty())
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.riskBand").value("MEDIUM"))
				.andExpect(jsonPath("$.offer.interestRate").value(13.5))
				.andExpect(jsonPath("$.offer.tenureMonths").value(36))
				.andExpect(jsonPath("$.offer.emi").value(16967.64))
				.andExpect(jsonPath("$.offer.totalPayable").value(610835.04))
				.andExpect(jsonPath("$.rejectionReasons").doesNotExist());

		assertThat(loanApplicationAuditRepository.count()).isEqualTo(1);
	}

	@Test
	void shouldRejectWhenOfferEmiExceedsFiftyPercentOfIncome() throws Exception {
		String payload = """
				{
				  "applicant": {
				    "name": "Rohit Kumar",
				    "age": 30,
				    "monthlyIncome": 33000,
				    "employmentType": "SALARIED",
				    "creditScore": 720
				  },
				  "loan": {
				    "amount": 500000,
				    "tenureMonths": 36,
				    "purpose": "PERSONAL"
				  }
				}
				""";

		mockMvc.perform(post("/applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.applicationId").isNotEmpty())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.riskBand").value(nullValue()))
				.andExpect(jsonPath("$.offer").doesNotExist())
				.andExpect(jsonPath("$.rejectionReasons[0]").value("EMI_EXCEEDS_50_PERCENT_OFFER_LIMIT"));

		assertThat(loanApplicationAuditRepository.count()).isEqualTo(1);
	}

	@Test
	void shouldReturnBadRequestForInvalidPayload() throws Exception {
		String payload = """
				{
				  "applicant": {
				    "name": "",
				    "age": 20,
				    "monthlyIncome": 0,
				    "employmentType": "SALARIED",
				    "creditScore": 250
				  },
				  "loan": {
				    "amount": 9999,
				    "tenureMonths": 3,
				    "purpose": "PERSONAL"
				  }
				}
				""";

		mockMvc.perform(post("/applications")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(content().string(containsString("applicant.name: name is required")))
				.andExpect(content().string(containsString("applicant.age: age must be between 21 and 60")))
				.andExpect(content().string(containsString("applicant.monthlyIncome: monthlyIncome must be greater than 0")))
				.andExpect(content().string(containsString("applicant.creditScore: creditScore must be between 300 and 900")))
				.andExpect(content().string(containsString("loan.amount: amount must be between 10000 and 5000000")))
				.andExpect(content().string(containsString("loan.tenureMonths: tenureMonths must be between 6 and 360")));

		assertThat(loanApplicationAuditRepository.count()).isZero();
	}
}
