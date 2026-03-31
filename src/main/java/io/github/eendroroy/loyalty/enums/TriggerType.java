package io.github.eendroroy.loyalty.enums;

public enum TriggerType {
    FILE_WATCHER,   // FILE source — triggered when the source file changes on disk
    INSTANT         // WEBHOOK source — data is pushed in real time by an external service
}

