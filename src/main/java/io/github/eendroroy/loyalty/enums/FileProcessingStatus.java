package io.github.eendroroy.loyalty.enums;

public enum FileProcessingStatus {
    IN_PROGRESS,  // claimed by an instance, currently being parsed
    COMPLETED,    // successfully ingested — will not be re-processed
    FAILED        // parsing failed — eligible for retry
}

