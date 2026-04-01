package io.github.eendroroy.loyalty.rule.ast;

/**
 * Refers to a specific field on a named data source.
 *
 * <p>In rule expressions this is written as {@code sourceName.fieldName},
 * e.g. {@code transaction.amount}.
 *
 * @param sourceName name of the {@link io.github.eendroroy.loyalty.entity.DataSource}
 * @param fieldName  field alias / schema-field name on that source
 */
public record FieldRef(String sourceName, String fieldName) {

    @Override
    public String toString() {
        return sourceName + "." + fieldName;
    }
}

