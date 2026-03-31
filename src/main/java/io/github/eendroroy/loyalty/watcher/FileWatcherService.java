package io.github.eendroroy.loyalty.watcher;

import io.github.eendroroy.loyalty.event.DataSourceFileDeletedEvent;
import io.github.eendroroy.loyalty.event.DataSourceFileSavedEvent;
import io.github.eendroroy.loyalty.repository.DataSourceFileRepository;
import io.github.eendroroy.loyalty.service.DataPullService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Watches the directories of all registered {@link io.github.eendroroy.loyalty.entity.DataSourceFile}
 * entries for file-system changes and delegates ingestion to {@link DataPullService}.
 *
 * <p>Each {@code DataSourceFile} is tracked by its primary key in {@code watchKeys}
 * (the NIO {@link WatchKey}) and {@code watchedDirs} (the resolved parent directory).
 * On a {@code ENTRY_CREATE} or {@code ENTRY_MODIFY} event the changed file path is
 * passed directly to {@link DataPullService#pull(Long, Path)} which applies
 * distributed deduplication before parsing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileWatcherService implements DisposableBean {

    private final DataSourceFileRepository fileRepository;
    private final DataPullService          dataPullService;

    private WatchService watchService;
    private Thread       watchThread;
    private final Map<Long, WatchKey> watchKeys   = new ConcurrentHashMap<>();
    private final Map<Long, Path>     watchedDirs = new ConcurrentHashMap<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            fileRepository.findAllWithDataSourceAndFields()
                    .forEach(f -> register(f.getId(), f.getFilePath()));
            startWatchThread();
            log.info("File watcher initialised — watching {} path(s).", watchKeys.size());
        } catch (IOException e) {
            log.error("Failed to initialise file watcher", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (watchThread != null) watchThread.interrupt();
        if (watchService != null) watchService.close();
    }

    // ── Event listeners ───────────────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileSaved(DataSourceFileSavedEvent event) {
        deregister(event.dataSourceFileId());
        register(event.dataSourceFileId(), event.filePath());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFileDeleted(DataSourceFileDeletedEvent event) {
        deregister(event.dataSourceFileId());
    }

    // ── Registration ──────────────────────────────────────────────────────────

    public void register(Long fileId, String filePathStr) {
        if (watchService == null) return;
        try {
            var filePath = Paths.get(filePathStr);
            var dir = Files.isDirectory(filePath) ? filePath : filePath.getParent();
            if (dir == null || !Files.exists(dir)) {
                log.warn("Watch directory does not exist for file id={}: {}", fileId, dir);
                return;
            }
            var key = dir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            watchKeys.put(fileId, key);
            watchedDirs.put(fileId, dir);
            log.info("Watching '{}' for DataSourceFile id={}", dir, fileId);
        } catch (IOException e) {
            log.error("Failed to register watcher for DataSourceFile id={}: {}", fileId, e.getMessage());
        }
    }

    public void deregister(Long fileId) {
        var key = watchKeys.remove(fileId);
        watchedDirs.remove(fileId);
        if (key != null) {
            key.cancel();
            log.info("Deregistered watcher for DataSourceFile id={}", fileId);
        }
    }

    /** Used by MonitorService to report live watcher status. */
    public boolean isWatching(Long fileId) {
        return watchKeys.containsKey(fileId);
    }

    // ── Watch thread ──────────────────────────────────────────────────────────

    private void startWatchThread() {
        watchThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    var key = watchService.take();
                    var fileId = watchKeys.entrySet().stream()
                            .filter(e -> e.getValue().equals(key))
                            .map(Map.Entry::getKey)
                            .findFirst().orElse(null);

                    key.pollEvents().forEach(event -> {
                        var changedName = (Path) event.context();
                        log.info("File event [{}] for DataSourceFile id={}: {}",
                                event.kind().name(), fileId, changedName);
                        if (fileId != null) {
                            var dir = watchedDirs.get(fileId);
                            var changedFile = dir != null ? dir.resolve(changedName) : null;
                            var result = dataPullService.pull(fileId, changedFile);
                            log.info("Pull result for DataSourceFile id={}: {} ingested, {} skipped, {} errors",
                                    fileId, result.rowsIngested(), result.rowsSkipped(), result.errors());
                        }
                    });
                    key.reset();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "loyalty-file-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
    }
}
