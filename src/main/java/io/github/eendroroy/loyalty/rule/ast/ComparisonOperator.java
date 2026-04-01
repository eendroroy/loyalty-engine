package io.github.eendroroy.loyalty.rule.ast;

/** Comparison operators supported in rule conditions. */
public enum ComparisonOperator {
    GT,          // >
    LT,          // <
    GTE,         // >=
    LTE,         // <=
    EQ,          // =
    NEQ,         // !=
    CONTAINS,    // CONTAINS (string)
    STARTS_WITH, // STARTS_WITH (string)
    ENDS_WITH    // ENDS_WITH (string)
}

