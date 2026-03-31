package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.entity.DataSource;
import io.github.eendroroy.loyalty.entity.DataSourceFile;
import io.github.eendroroy.loyalty.entity.DataSourceSchemaField;
import io.github.eendroroy.loyalty.entity.DataSourceWebhook;
import io.github.eendroroy.loyalty.entity.DataSourceWebhookProperty;
import io.github.eendroroy.loyalty.event.DataSourceFileDeletedEvent;
import io.github.eendroroy.loyalty.event.DataSourceFileSavedEvent;
import io.github.eendroroy.loyalty.event.DataSourceSavedEvent;
import io.github.eendroroy.loyalty.repository.DataSourceFieldRepository;
import io.github.eendroroy.loyalty.repository.DataSourceFileRepository;
import io.github.eendroroy.loyalty.repository.DataSourceRepository;
import io.github.eendroroy.loyalty.repository.DataSourceSchemaFieldRepository;
import io.github.eendroroy.loyalty.repository.DataSourceWebhookPropertyRepository;
import io.github.eendroroy.loyalty.repository.DataSourceWebhookRepository;
import io.github.eendroroy.loyalty.repository.FileProcessingLogRepository;
import io.github.eendroroy.loyalty.service.DataSourceService;
import io.github.eendroroy.loyalty.service.DataSourceTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements DataSourceService {

    private final DataSourceRepository           repository;
    private final DataSourceFieldRepository      fieldRepository;
    private final DataSourceFileRepository       fileRepository;
    private final DataSourceWebhookRepository    webhookRepository;
    private final DataSourceWebhookPropertyRepository webhookPropertyRepository;
    private final DataSourceSchemaFieldRepository schemaFieldRepository;
    private final FileProcessingLogRepository    logRepository;
    private final DataSourceTableService         tableService;
    private final ApplicationEventPublisher      eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<DataSource> findAllWithDestinationTable() {
        var result = repository.findAllWithFilesAndTable();
        result.forEach(ds -> fileRepository.findByDataSourceIdWithFields(ds.getId()));
        repository.findAllWithWebhooksAndTable();
        result.forEach(ds -> webhookRepository.findAllWithPropertiesByDataSourceId(ds.getId()));
        repository.findAllWithSchemaFields();
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataSource> findAll() {
        var result = repository.findAllWithFiles();
        result.forEach(ds -> fileRepository.findByDataSourceIdWithFields(ds.getId()));
        repository.findAllWithWebhooks();
        result.forEach(ds -> webhookRepository.findAllWithPropertiesByDataSourceId(ds.getId()));
        repository.findAllWithSchemaFields();
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataSource> findAll(Specification<DataSource> spec) {
        return repository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DataSource> findById(Long id) {
        // Store result from pass 1 — avoids a redundant 6th DB round-trip
        var result = repository.findByIdWithFiles(id);
        result.ifPresent(ds -> fileRepository.findByDataSourceIdWithFields(ds.getId()));
        repository.findByIdWithWebhooks(id);
        webhookRepository.findAllWithPropertiesByDataSourceId(id);
        repository.findByIdWithSchemaFields(id);
        return result;
    }

    @Override
    @Transactional
    public DataSource save(DataSource entity) {
        var saved = repository.save(entity);

        // Auto-create a disabled webhook if one doesn't exist
        if (saved.getWebhook() == null) {
            var webhook = new DataSourceWebhook();
            webhook.setDataSource(saved);
            webhook.setEnabled(false);
            var savedWebhook = webhookRepository.save(webhook);
            saved.setWebhook(savedWebhook);
        }

        // Sync webhook properties to schema fields
        syncWebhookPropertiesToSchemaFields(saved.getId());

        // Eagerly create the destination table so it exists immediately after the source is saved.
        // createDestinationTableIfNotExists is idempotent (CREATE TABLE IF NOT EXISTS).
        if (saved.getDestinationTable() != null && !saved.getDestinationTable().isBlank()) {
            tableService.createDestinationTableIfNotExists(saved);
        }

        saved.getFiles().forEach(f ->
                eventPublisher.publishEvent(new DataSourceFileSavedEvent(f.getId(), f.getFilePath())));
        eventPublisher.publishEvent(new DataSourceSavedEvent(saved));
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("Deletion of DataSource is disabled. Use archive instead.");
    }

    @Override
    @Transactional
    public Optional<DataSource> update(Long id, Consumer<DataSource> scalarUpdater,
                                       List<DataSourceFile> newFiles,
                                       List<DataSourceWebhook> newWebhooks,
                                       List<DataSourceSchemaField> newSchemaFields) {
        return repository.findById(id).map(existing -> {
            scalarUpdater.accept(existing);

            if (newSchemaFields != null) {
                schemaFieldRepository.deleteByDataSourceId(id);
                newSchemaFields.forEach(f -> f.setDataSource(existing));
                schemaFieldRepository.saveAll(newSchemaFields);
                // Sync webhook properties to the new schema fields
                syncWebhookPropertiesToSchemaFields(id);
            }

            if (newFiles != null) {
                var oldFileIds = fileRepository.findByDataSourceId(id).stream()
                        .map(DataSourceFile::getId).toList();
                if (!oldFileIds.isEmpty()) {
                    logRepository.deleteByDataSourceFileIdIn(oldFileIds);
                }
                oldFileIds.forEach(fieldRepository::deleteByDataSourceFileId);
                fileRepository.deleteByDataSourceId(id);
                oldFileIds.forEach(fid -> eventPublisher.publishEvent(new DataSourceFileDeletedEvent(fid)));

                newFiles.forEach(f -> {
                    f.setDataSource(existing);
                    var savedFile = fileRepository.save(f);
                    if (f.getFields() != null && !f.getFields().isEmpty()) {
                        f.getFields().forEach(field -> field.setDataSourceFile(savedFile));
                        fieldRepository.saveAll(f.getFields());
                    }
                    eventPublisher.publishEvent(
                            new DataSourceFileSavedEvent(savedFile.getId(), savedFile.getFilePath()));
                });
            }

            if (newWebhooks != null) {
                // One webhook per source — update enabled status from first entry
                var existingWebhook = webhookRepository.findByDataSourceId(id);
                if (existingWebhook.isPresent() && !newWebhooks.isEmpty()) {
                    var webhook = existingWebhook.get();
                    webhook.setEnabled(newWebhooks.getFirst().isEnabled());
                    webhookRepository.save(webhook);
                }
            }

            var saved = repository.save(existing);

            // Eagerly ensure the destination table exists after every update.
            // idempotent — safe to call even if the table already exists.
            if (saved.getDestinationTable() != null && !saved.getDestinationTable().isBlank()) {
                tableService.createDestinationTableIfNotExists(saved);
            }

            eventPublisher.publishEvent(new DataSourceSavedEvent(saved));
            return saved;
        });
    }

    /**
     * Synchronizes webhook properties with the data source's current schema fields.
     * Deletes existing properties and recreates them to match the schema fields exactly.
     */
    private void syncWebhookPropertiesToSchemaFields(Long dataSourceId) {
        var webhookOpt = webhookRepository.findByDataSourceId(dataSourceId);
        if (webhookOpt.isEmpty()) return;

        var wh = webhookOpt.get();
        webhookPropertyRepository.deleteByWebhookId(wh.getId());

        var schemaFields = schemaFieldRepository.findByDataSourceId(dataSourceId);
        var properties = schemaFields.stream().map(sf -> {
            var property = new DataSourceWebhookProperty();
            property.setWebhook(wh);
            property.setName(sf.getName());
            property.setFieldAlias(sf.getName());
            property.setDataType(sf.getDataType());
            property.setDescription(sf.getDescription());
            return property;
        }).toList();
        webhookPropertyRepository.saveAll(properties);
    }

    @Override
    @Transactional
    public void archiveById(Long id) {
        repository.findById(id).ifPresent(ds -> {
            ds.setArchived(true);
            repository.save(ds);
            fileRepository.findByDataSourceId(id).forEach(f ->
                    eventPublisher.publishEvent(new DataSourceFileDeletedEvent(f.getId())));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataSource> findArchived() {
        return repository.findByArchived(true);
    }

    @Override
    @Transactional
    public void purgeArchivedById(Long id) {
        var dsOpt = repository.findById(id);
        if (dsOpt.isEmpty()) return;

        var ds = dsOpt.get();
        if (!ds.isArchived()) {
            throw new IllegalStateException("Only archived sources can be purged.");
        }
        // Targeted existence check — avoids loading all active sources just to scan them
        if (ds.getDestinationTable() != null && !ds.getDestinationTable().isBlank()
                && repository.existsByDestinationTableAndIdNotAndArchivedFalse(
                        ds.getDestinationTable(), id)) {
            throw new IllegalStateException("Destination table is still used by an active source.");
        }
        var fileIds = fileRepository.findByDataSourceId(id).stream()
                .map(DataSourceFile::getId).toList();
        if (!fileIds.isEmpty()) {
            logRepository.deleteByDataSourceFileIdIn(fileIds);
        }
        repository.deleteById(id);
    }
}
