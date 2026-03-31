package io.github.eendroroy.loyalty.event;

/**
 * Published after a {@link io.github.eendroroy.loyalty.entity.DataSourceFile} is
 * saved (created or updated).  Carries the minimal information the
 * {@link io.github.eendroroy.loyalty.watcher.FileWatcherService} needs to
 * register the directory without touching a lazy collection after the
 * transaction has committed.
 *
 * @param dataSourceFileId primary key of the saved file config
 * @param filePath         the file (or directory) path to watch
 */
public record DataSourceFileSavedEvent(Long dataSourceFileId, String filePath) {
}

