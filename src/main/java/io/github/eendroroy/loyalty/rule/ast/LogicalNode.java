package io.github.eendroroy.loyalty.rule.ast;

/**
 * Sealed base type for nodes in the condition tree of a parsed rule.
 *
 * <p>The three permitted subtypes are:
 * <ul>
 *   <li>{@link AndNode} — both children must evaluate to {@code true}</li>
 *   <li>{@link OrNode}  — at least one child must evaluate to {@code true}</li>
 *   <li>{@link LeafNode} — a single {@link Condition} evaluated against a record</li>
 * </ul>
 */
public sealed interface LogicalNode permits AndNode, OrNode, LeafNode {
}

