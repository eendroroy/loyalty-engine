package io.github.eendroroy.loyalty.rule;

import io.github.eendroroy.loyalty.model.IngestedRecord;
import io.github.eendroroy.loyalty.rule.ast.AndNode;
import io.github.eendroroy.loyalty.rule.ast.ComparisonOperator;
import io.github.eendroroy.loyalty.rule.ast.Condition;
import io.github.eendroroy.loyalty.rule.ast.LeafNode;
import io.github.eendroroy.loyalty.rule.ast.LogicalNode;
import io.github.eendroroy.loyalty.rule.ast.OrNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Evaluates a parsed rule's {@link LogicalNode} condition tree against a single
 * {@link IngestedRecord}.
 *
 * <h3>Source matching</h3>
 * Each {@link io.github.eendroroy.loyalty.rule.ast.Condition} references a specific data source
 * by name (e.g. {@code transaction.amount}).  If the record's {@code sourceName} does not match
 * the condition's source, the leaf evaluates to {@code false}.  This means AND conditions that
 * span multiple data sources will never fire from a single-source ingestion batch — which is the
 * expected behaviour for per-record evaluation.
 *
 * <h3>Type coercion</h3>
 * The right-hand side {@code rawValue} is always a string token from the parser.
 * It is coerced at evaluation time to match the actual Java type of the field value:
 * {@code Long}, {@link BigDecimal}, {@link LocalDate}, {@code Boolean}, or {@code String}.
 */
@Slf4j
@Component
public class RuleEvaluator {

    /**
     * Evaluates the condition tree against the given record.
     *
     * @param node   root of the logical condition tree
     * @param record the ingested record to test
     * @return {@code true} if the record satisfies the condition
     */
    public boolean evaluate(LogicalNode node, IngestedRecord record) {
        return switch (node) {
            case AndNode and   -> evaluate(and.left(), record) && evaluate(and.right(), record);
            case OrNode  or    -> evaluate(or.left(),  record) || evaluate(or.right(),  record);
            case LeafNode leaf -> evaluateCondition(leaf.condition(), record);
        };
    }

    // ── Condition evaluation ──────────────────────────────────────────────────

    private boolean evaluateCondition(Condition condition, IngestedRecord record) {
        // The record must belong to the data source referenced in the condition
        if (!record.sourceName().equalsIgnoreCase(condition.fieldRef().sourceName())) {
            return false;
        }

        var fieldValue = record.fields().get(condition.fieldRef().fieldName());
        if (fieldValue == null) {
            return false;
        }

        try {
            return compare(fieldValue, condition.operator(), condition.rawValue());
        } catch (Exception e) {
            log.warn("Evaluation error for condition '{}': {}", condition, e.getMessage());
            return false;
        }
    }

    // ── Type-aware comparison ─────────────────────────────────────────────────

    private boolean compare(Object fieldValue, ComparisonOperator op, String rawValue) {
        return switch (fieldValue) {
            case Long l        -> applyNumeric(l.compareTo(parseLong(rawValue)), op);
            case BigDecimal bd -> applyNumeric(bd.compareTo(new BigDecimal(rawValue)), op);
            case LocalDate ld  -> applyNumeric(ld.compareTo(LocalDate.parse(rawValue)), op);
            case Boolean b     -> compareBoolean(b, op, rawValue);
            case String s      -> compareString(s, op, rawValue);
            default -> {
                // Fall back to BigDecimal if the raw value looks numeric, else String
                try {
                    yield applyNumeric(new BigDecimal(fieldValue.toString())
                            .compareTo(new BigDecimal(rawValue)), op);
                } catch (NumberFormatException ignored) {
                    yield compareString(fieldValue.toString(), op, rawValue);
                }
            }
        };
    }

    private long parseLong(String rawValue) {
        // Accept "30" or "30.0" (truncate decimal for integer fields)
        return rawValue.contains(".")
                ? new BigDecimal(rawValue).longValueExact()
                : Long.parseLong(rawValue);
    }

    private boolean applyNumeric(int cmp, ComparisonOperator op) {
        return switch (op) {
            case GT  -> cmp > 0;
            case LT  -> cmp < 0;
            case GTE -> cmp >= 0;
            case LTE -> cmp <= 0;
            case EQ  -> cmp == 0;
            case NEQ -> cmp != 0;
            default  -> throw new UnsupportedOperationException(
                    "Operator " + op + " is not applicable to numeric/date values");
        };
    }

    private boolean compareBoolean(boolean fieldVal, ComparisonOperator op, String rawValue) {
        var rhs = Boolean.parseBoolean(rawValue);
        return switch (op) {
            case EQ  -> fieldVal == rhs;
            case NEQ -> fieldVal != rhs;
            default  -> throw new UnsupportedOperationException(
                    "Operator " + op + " is not applicable to boolean values");
        };
    }

    private boolean compareString(String fieldVal, ComparisonOperator op, String rawValue) {
        return switch (op) {
            case EQ          -> fieldVal.equals(rawValue);
            case NEQ         -> !fieldVal.equals(rawValue);
            case CONTAINS    -> fieldVal.contains(rawValue);
            case STARTS_WITH -> fieldVal.startsWith(rawValue);
            case ENDS_WITH   -> fieldVal.endsWith(rawValue);
            default          -> throw new UnsupportedOperationException(
                    "Operator " + op + " is not applicable to string values");
        };
    }
}

