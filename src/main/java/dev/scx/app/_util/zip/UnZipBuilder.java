package dev.scx.app._util.zip;

import dev.scx.app._util.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipInputStream;

/// UnZipBuilder
///
/// @author scx567888
/// @version 0.0.1
public final class UnZipBuilder {

    private final InputStream source;

    private Path path = null;

    public UnZipBuilder(InputStream source) {
        this.source = source;
    }

    public UnZipBuilder(Path path) throws IOException {
        this(Files.newInputStream(path));
        this.path = path;
    }

    public UnZipBuilder(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }

    /// 解压
    ///
    /// @param outputPath 解压到的目录
    /// @param zipOptions a
    /// @throws IOException a
    public void toFile(Path outputPath, ZipOptions zipOptions) throws IOException {
        Files.createDirectories(outputPath);
        var rootPath = getRootPath(zipOptions);
        try (var zis = new ZipInputStream(this.source, zipOptions.charset())) {
            // 遍历每一个文件
            var zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                var unzipFilePath = outputPath.resolve(rootPath + zipEntry.getName());
                if (zipEntry.isDirectory()) { // 文件夹
                    Files.createDirectories(unzipFilePath);
                } else { // 文件
                    Files.createDirectories(unzipFilePath.getParent());
                    Files.copy(zis, unzipFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
        }
    }

    public void toFile(Path outputPath) throws IOException {
        toFile(outputPath, new ZipOptions());
    }

    private String getRootPath(ZipOptions zipOptions) {
        if (zipOptions.includeRoot() && this.path != null) {
            var fileName = this.path.getFileName().toString();
            var fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName);
            return URIUtils.addSlashEnd(fileNameWithoutExtension);
        }
        return "";
    }

}
