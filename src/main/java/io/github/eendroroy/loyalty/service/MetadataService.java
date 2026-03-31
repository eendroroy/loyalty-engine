package io.github.eendroroy.loyalty.service;

import io.github.eendroroy.loyalty.dto.response.MetadataResponse;

public interface MetadataService {
    /**
     * Returns a consolidated snapshot of every configured data source and its
     * field aliases.  Used by the frontend to populate rule-expression autocomplete.
     */
    MetadataResponse getMetadata();
}

