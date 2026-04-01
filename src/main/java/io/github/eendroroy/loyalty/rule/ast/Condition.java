package io.github.eendroroy.loyalty.rule.ast;

/**
 * A single leaf condition in a rule expression.
 *
 * <p>Example: {@code transaction.amount > 50}
 *
 * @param fieldRef the data source field being tested
 * @param operator the comparison to apply
 * @param rawValue the right-hand side value as a raw string token;
 *                 type-coerced at evaluation time based on the actual field value type
 */
public record Condition(FieldRef fieldRef, ComparisonOperator operator, String rawValue) {

    @Override
    public String toString() {
        return fieldRef + " " + operator + " " + rawValue;
    }
}

