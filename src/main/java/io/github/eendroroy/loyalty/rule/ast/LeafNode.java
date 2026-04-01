package io.github.eendroroy.loyalty.rule.ast;

/** Terminal node wrapping a single {@link Condition}. */
public record LeafNode(Condition condition) implements LogicalNode {
}

