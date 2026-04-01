package io.github.eendroroy.loyalty.controller;

import io.github.eendroroy.loyalty.rule.exception.RuleParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates application exceptions to structured HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converts a {@link RuleParseException} (invalid rule expression) to
     * {@code 400 Bad Request} with a Problem Detail body.
     */
    @ExceptionHandler(RuleParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleRuleParseException(RuleParseException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Rule Expression Parse Error");
        return detail;
    }
}

