package io.github.eendroroy.loyalty.enums;

/**
 * Content type for data pushed to a webhook endpoint.
 * Controls how the ingestion controller parses the incoming request body.
 */
public enum WebhookContentType {
    /** JSON body — {@code Content-Type: application/json} (default). */
    APPLICATION_JSON
}

