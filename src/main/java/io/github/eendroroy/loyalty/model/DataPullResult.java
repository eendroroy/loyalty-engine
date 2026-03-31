package io.github.eendroroy.loyalty.model;

/**
 * Summary returned by {@link io.github.eendroroy.loyalty.service.DataPullService}
 * after a data ingestion run.
 *
 * @param rowsIngested number of rows successfully parsed and published
 * @param rowsSkipped  number of rows skipped due to missing/empty values
 * @param errors       number of rows that could not be parsed (logged as warnings)
 */
public record DataPullResult(long rowsIngested, long rowsSkipped, long errors) {

    /** Convenience constructor for a fully successful pull. */
    public static DataPullResult success(long rows) {
        return new DataPullResult(rows, 0, 0);
    }

    /** Convenience constructor for a pull that failed before any row was read. */
    public static DataPullResult failed() {
        return new DataPullResult(0, 0, 1);
    }
}

