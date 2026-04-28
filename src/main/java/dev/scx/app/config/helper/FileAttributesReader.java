package dev.scx.app.config.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// 通过 FileWatcher 主动更新 代替高频的 Files.readAttributes 查询
///
/// @author scx567888
/// @version 0.0.1
public class FileAttributesReader {

    //为了解决 ConcurrentHashMap 无法存储空值的问题
    private static final NotExist NOT_EXIST = new NotExist();

    private final Path target;
    private final FileWatcher fileWatcher;
    private final Map<Path, BasicFileAttributes> cache;

    public FileAttributesReader(Path target) throws IOException {
        this.target = target;
        this.fileWatcher = new FileWatcher(target).listener(this::onChange).start();
        this.cache = new ConcurrentHashMap<>();
    }

    private void onChange(FileWatcher.ChangeEvent event) {
        try {
            if (event.type() == FileWatcher.ChangeEventType.DELETED) {
                cache.put(event.target(), NOT_EXIST);
            } else {
                cache.put(event.target(), Files.readAttributes(event.target(), BasicFileAttributes.class));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// 这里为了性能考虑并没有在 方法内部判断
    /// 但是请保证 参数 path 一定是 target 的子目录或者子文件 否则将无法正确判断文件变化
    ///
    /// @param path path
    /// @return 文件内容 (可能为 null )
    /// @throws IOException a
    public BasicFileAttributes get(Path path) throws IOException {
        var f = cache.computeIfAbsent(path, c -> {
            try {
                return Files.readAttributes(c, BasicFileAttributes.class);
            } catch (NoSuchFileException e) {
                return NOT_EXIST;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        if (f == NOT_EXIST) {
            return null;
        }
        return f;
    }

    private static class NotExist implements BasicFileAttributes {

        @Override
        public FileTime lastModifiedTime() {
            return null;
        }

        @Override
        public FileTime lastAccessTime() {
            return null;
        }

        @Override
        public FileTime creationTime() {
            return null;
        }

        @Override
        public boolean isRegularFile() {
            return false;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public boolean isSymbolicLink() {
            return false;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public Object fileKey() {
            return null;
        }
    }

}
