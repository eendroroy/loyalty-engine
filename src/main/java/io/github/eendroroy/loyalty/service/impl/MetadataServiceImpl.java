package io.github.eendroroy.loyalty.service.impl;

import io.github.eendroroy.loyalty.dto.response.DataSourceMetadataResponse;
import io.github.eendroroy.loyalty.dto.response.FieldMetadataResponse;
import io.github.eendroroy.loyalty.dto.response.MetadataResponse;
import io.github.eendroroy.loyalty.service.DataSourceService;
import io.github.eendroroy.loyalty.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {

    private final DataSourceService dataSourceService;

    @Override
    @Transactional(readOnly = true)
    public MetadataResponse getMetadata() {
        var sources = dataSourceService.findAll().stream()
                .map(ds -> {
                    // Prefer schema fields (canonical); fall back to file-level fields
                    List<FieldMetadataResponse> fields;
                    if (ds.getSchemaFields() != null && !ds.getSchemaFields().isEmpty()) {
                        fields = ds.getSchemaFields().stream()
                                .map(f -> FieldMetadataResponse.builder()
                                        .alias(f.getName())
                                        .fieldName(f.getName())
                                        .dataType(f.getDataType())
                                        .description(f.getDescription())
                                        .build())
                                .toList();
                    } else {
                        fields = ds.getAllFields().stream()
                                .map(f -> FieldMetadataResponse.builder()
                                        .alias(f.getFieldAlias())
                                        .fieldName(f.getFieldName())
                                        .dataType(f.getDataType())
                                        .columnNumber(f.getColumnNumber())
                                        .description(f.getDescription())
                                        .build())
                                .toList();
                    }
                    return DataSourceMetadataResponse.builder()
                            .id(ds.getId())
                            .name(ds.getName())
                            .fields(fields)
                            .build();
                })
                .toList();

        var totalFields = sources.stream().mapToInt(s -> s.getFields().size()).sum();

        return MetadataResponse.builder()
                .dataSources(sources)
                .totalFields(totalFields)
                .build();
    }
}
