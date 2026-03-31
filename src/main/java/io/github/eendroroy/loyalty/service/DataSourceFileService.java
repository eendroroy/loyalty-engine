package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.DataSourceField;
import io.github.eendroroy.loyalty.entity.DataSourceFile;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface DataSourceFileService {
    List<DataSourceFile> findByDataSourceId(Long dataSourceId);
    Optional<DataSourceFile> findById(Long id);
    DataSourceFile save(DataSourceFile entity);
    void deleteById(Long id);

    /**
     * Atomically updates a file's scalar fields and — when {@code newFields} is non-null —
     * replaces all field definitions for the file using bulk DELETE + INSERT within a single
     * transaction.  Avoids the unique-constraint violation that orphan-removal would cause.
     *
     * @param fileId        primary key of the file to update
     * @param scalarUpdater consumer that applies scalar changes to the managed entity
     * @param newFields     replacement field list, or {@code null} to leave fields unchanged
     * @return the updated file loaded with its field associations, or empty if not found
     */
    Optional<DataSourceFile> updateWithFields(Long fileId, Consumer<DataSourceFile> scalarUpdater,
                                              List<DataSourceField> newFields);
}

