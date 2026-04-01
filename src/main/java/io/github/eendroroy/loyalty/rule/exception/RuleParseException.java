package io.github.eendroroy.loyalty.rule.exception;

/**
 * Thrown when a rule expression cannot be parsed due to a syntax or semantic error.
 */
public class RuleParseException extends RuntimeException {

    public RuleParseException(String message) {
        super(message);
    }

    public RuleParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

