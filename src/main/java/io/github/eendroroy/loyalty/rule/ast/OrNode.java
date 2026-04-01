package io.github.eendroroy.loyalty.rule.ast;

/** Logical OR node — at least one of {@code left} or {@code right} must be true. */
public record OrNode(LogicalNode left, LogicalNode right) implements LogicalNode {
}

