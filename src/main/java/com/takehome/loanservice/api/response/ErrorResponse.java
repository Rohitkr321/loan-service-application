package com.takehome.loanservice.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
		OffsetDateTime timestamp,
		int status,
		String error,
		String message,
		List<String> details) {
}
