package dev.scx.app._util;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

/// 文件监听器
///
/// @author scx567888
/// @version 0.0.1
public final class FileWatcher {

    private final WatchService watchService;
    private final Path target;
    private final Path watchTarget;
    private final boolean isFile;
    private Thread watchThread;
    private Consumer<ChangeEvent> listener;

    public FileWatcher(Path target) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.target = target;
        this.isFile = !Files.isDirectory(target);
        this.watchTarget = isFile ? target.getParent() : target;
        watchTarget.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    public void _do() {
        while (true) {
            WatchKey watchKey;
            try {
                watchKey = watchService.take();
            } catch (InterruptedException e) {
                //中断则退出
                break;
            }
            var watchEvents = watchKey.pollEvents();
            for (var event : watchEvents) {
                var eventPath = (Path) event.context();
                var eventKind = event.kind();

                //使用全路径方便处理
                eventPath = watchTarget.resolve(eventPath);

                //如果监听的是单个文件 但是发生变化的并不是这个文件 我们跳过
                if (isFile && !target.equals(eventPath)) {
                    continue;
                }
                //调用监听
                _callListener(eventPath, eventKind);
            }
            // reset the key
            var valid = watchKey.reset();
            if (!valid) {
                break;
            }
        }
    }

    public FileWatcher listener(Consumer<ChangeEvent> listener) {
        this.listener = listener;
        return this;
    }

    private void _callListener(Path target, WatchEvent.Kind<?> kind) {
        if (listener != null) {
            this.listener.accept(new ChangeEvent(target, ChangeEventType.of(kind)));
        }
    }

    public FileWatcher start() {
        this.watchThread = Thread.ofPlatform().name("FileWatcher-WatchThread").start(this::_do);
        return this;
    }

    public void stop() {
        if (this.watchThread != null) {
            this.watchThread.interrupt();
            this.watchThread = null;
        }
        try {
            watchService.close();
        } catch (IOException _) {

        }
    }

    public enum ChangeEventType {

        MODIFY,

        DELETED,

        CREATED;

        public static ChangeEventType of(WatchEvent.Kind<?> t) {
            if (t == ENTRY_CREATE) {
                return CREATED;
            }
            if (t == ENTRY_MODIFY) {
                return MODIFY;
            }
            if (t == ENTRY_DELETE) {
                return DELETED;
            }
            throw new IllegalArgumentException("未知类型 : " + t.toString());
        }

    }

    public record ChangeEvent(Path target, ChangeEventType type) {

    }

}
