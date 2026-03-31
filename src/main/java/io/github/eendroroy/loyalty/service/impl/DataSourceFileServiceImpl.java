package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSourceField;
import io.github.eendroroy.loyalty.entity.DataSourceFile;
import io.github.eendroroy.loyalty.event.DataSourceFileDeletedEvent;
import io.github.eendroroy.loyalty.event.DataSourceFileSavedEvent;
import io.github.eendroroy.loyalty.repository.DataSourceFieldRepository;
import io.github.eendroroy.loyalty.repository.DataSourceFileRepository;
import io.github.eendroroy.loyalty.repository.FileProcessingLogRepository;
import io.github.eendroroy.loyalty.service.DataSourceFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class DataSourceFileServiceImpl implements DataSourceFileService {

    private final DataSourceFileRepository repository;
    private final DataSourceFieldRepository fieldRepository;
    private final FileProcessingLogRepository logRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<DataSourceFile> findByDataSourceId(Long dataSourceId) {
        // Use JOIN FETCH to avoid LazyInitializationException on fields (OSIV is off)
        return repository.findByDataSourceIdWithFields(dataSourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DataSourceFile> findById(Long id) {
        // Use JOIN FETCH to avoid LazyInitializationException on fields (OSIV is off)
        return repository.findByIdWithDataSourceAndFields(id);
    }

    @Override
    @Transactional
    public DataSourceFile save(DataSourceFile entity) {
        var saved = repository.save(entity);
        eventPublisher.publishEvent(new DataSourceFileSavedEvent(saved.getId(), saved.getFilePath()));
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        logRepository.deleteByDataSourceFileId(id);
        repository.deleteById(id);
        eventPublisher.publishEvent(new DataSourceFileDeletedEvent(id));
    }

    /**
     * Updates scalar fields and optionally replaces all field definitions within a single
     * transaction.  Persists scalar changes first (saveAndFlush), then runs the JPQL bulk-delete
     * (clearAutomatically = true clears the L1 cache).  The entity is reloaded after the cache
     * clear before inserting new fields, so Hibernate does not attempt to re-cascade the stale
     * in-memory collection.
     */
    @Override
    @Transactional
    public Optional<DataSourceFile> updateWithFields(Long fileId,
                                                     Consumer<DataSourceFile> scalarUpdater,
                                                     List<DataSourceField> newFields) {
        var fileOpt = repository.findById(fileId);
        if (fileOpt.isEmpty()) return Optional.empty();

        var existing = fileOpt.get();
        scalarUpdater.accept(existing);
        repository.saveAndFlush(existing); // persist scalar changes before L1 cache clear

        if (newFields != null) {
            fieldRepository.deleteByDataSourceFileId(fileId); // clears L1 cache
            // Reload after cache was cleared so the entity is managed again
            var reloaded = repository.findById(fileId).orElseThrow();
            newFields.forEach(f -> f.setDataSourceFile(reloaded));
            fieldRepository.saveAll(newFields);
        }

        // Return entity with associations loaded via JOIN FETCH
        return repository.findByIdWithDataSourceAndFields(fileId);
    }
}
