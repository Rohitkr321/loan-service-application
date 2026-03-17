package com.takehome.loanservice.exception;

import com.takehome.loanservice.api.response.ErrorResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::formatFieldError)
				.distinct()
				.toList();

		return badRequest("Validation failed", details);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
		Throwable cause = ex.getCause();
		if (cause instanceof InvalidFormatException invalidFormatException) {
			return badRequest("Request body could not be parsed", List.of(buildInvalidFormatMessage(invalidFormatException)));
		}
		return badRequest("Request body could not be parsed", List.of("Malformed JSON request"));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		return badRequest("Validation failed", List.of(ex.getMessage()));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(NoResourceFoundException ex) {
		ErrorResponse response = new ErrorResponse(
				OffsetDateTime.now(ZoneOffset.UTC),
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				"Resource not found",
				List.of(ex.getMessage()));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		String supportedMethods = ex.getSupportedHttpMethods() == null || ex.getSupportedHttpMethods().isEmpty()
				? "No supported methods reported"
				: ex.getSupportedHttpMethods().stream()
						.map(Object::toString)
						.collect(Collectors.joining(", "));

		ErrorResponse response = new ErrorResponse(
				OffsetDateTime.now(ZoneOffset.UTC),
				HttpStatus.METHOD_NOT_ALLOWED.value(),
				HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
				"Method not allowed",
				List.of("Supported methods: " + supportedMethods));
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
		ErrorResponse response = new ErrorResponse(
				OffsetDateTime.now(ZoneOffset.UTC),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
				"Unexpected server error",
				List.of());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	private ResponseEntity<ErrorResponse> badRequest(String message, List<String> details) {
		ErrorResponse response = new ErrorResponse(
				OffsetDateTime.now(ZoneOffset.UTC),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				message,
				details);
		return ResponseEntity.badRequest().body(response);
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}

	private String buildInvalidFormatMessage(InvalidFormatException exception) {
		String fieldPath = exception.getPath()
				.stream()
				.map(reference -> {
					if (reference.getPropertyName() != null) {
						return reference.getPropertyName();
					}
					return reference.getIndex() >= 0 ? "[" + reference.getIndex() + "]" : null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.joining("."));

		if (exception.getTargetType().isEnum()) {
			String acceptedValues = Arrays.stream(exception.getTargetType().getEnumConstants())
					.map(Object::toString)
					.collect(Collectors.joining(", "));
			return "Invalid value '" + exception.getValue() + "' for field '" + fieldPath
					+ "'. Accepted values: " + acceptedValues;
		}

		return "Invalid value '" + exception.getValue() + "' for field '" + fieldPath + "'";
	}
}
