package io.github.eendroroy.loyalty.service;
import io.github.eendroroy.loyalty.dto.response.TableDataResponse;
import io.github.eendroroy.loyalty.entity.DataSource;
import io.github.eendroroy.loyalty.entity.DataSourceField;
import io.github.eendroroy.loyalty.model.IngestedRecord;
import java.util.List;
/**
 * Service for managing destination tables where ingested data is stored.
 */
public interface DataSourceTableService {
    void createDestinationTableIfNotExists(DataSource dataSource);
    void createDestinationTableIfNotExists(DataSource dataSource, List<DataSourceField> fields);
    int insertRecords(DataSource dataSource, List<IngestedRecord> records);
    int insertRecords(DataSource dataSource, List<IngestedRecord> records, List<DataSourceField> fields);
    List<IngestedRecord> findAllRecords(DataSource dataSource);
    void deleteAllRecords(DataSource dataSource);
    void dropDestinationTable(DataSource dataSource);
    TableDataResponse queryRecords(DataSource dataSource,
                                   String search,
                                   int page,
                                   int size,
                                   String sortBy,
                                   String sortDir);
}
