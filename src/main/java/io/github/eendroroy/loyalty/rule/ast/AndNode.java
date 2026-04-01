package io.github.eendroroy.loyalty.rule.ast;

/** Logical AND node — both {@code left} and {@code right} must be true. */
public record AndNode(LogicalNode left, LogicalNode right) implements LogicalNode {
}

