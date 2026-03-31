package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.entity.DataSourceWebhook;

import java.util.List;
import java.util.Optional;

public interface DataSourceWebhookService {
    List<DataSourceWebhook> findAllByDataSourceId(Long dataSourceId);
    Optional<DataSourceWebhook> findById(Long id);
    DataSourceWebhook save(DataSourceWebhook entity);
    void deleteByDataSourceId(Long dataSourceId);
    void deleteById(Long id);
}
