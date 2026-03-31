package io.github.eendroroy.loyalty.event;

/**
 * Published after a {@link io.github.eendroroy.loyalty.entity.DataSourceFile} is deleted.
 *
 * @param dataSourceFileId primary key of the deleted file config
 */
public record DataSourceFileDeletedEvent(Long dataSourceFileId) {
}

