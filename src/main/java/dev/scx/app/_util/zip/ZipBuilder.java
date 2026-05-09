package dev.scx.app._util.zip;


import dev.scx.app._util.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/// 简化 zip 的创建
///
/// @author scx567888
/// @version 0.0.1
public final class ZipBuilder {

    private final List<ZipBuilderItem> items = new ArrayList<>();

    public ZipBuilder() {

    }

    public ZipBuilder(ZipFile zipFile) throws IOException {
        var list = zipFile.stream().toList();
        for (var zipEntry : list) {
            var i = new ZipBuilderItem(zipEntry, zipFile);
            items.add(i);
        }
    }

    public ZipBuilder(Path path) throws IOException {
        this.put(path);
    }

    public ZipBuilder(Path path, ZipOptions zipOptions) throws IOException {
        this.put(path, zipOptions);
    }

    public ZipBuilder put(String zipPath) {
        items.add(new ZipBuilderItem(zipPath));
        return this;
    }

    public ZipBuilder put(Path path, ZipOptions zipOptions) throws IOException {
        items.add(new PathZipBuilderItem("", path, zipOptions));
        return this;
    }

    public ZipBuilder put(Path path) throws IOException {
        return this.put(path, new ZipOptions());
    }

    public ZipBuilder put(String zipPath, Path path, ZipOptions zipOptions) throws IOException {
        items.add(new PathZipBuilderItem(zipPath, path, zipOptions));
        return this;
    }

    public ZipBuilder put(String zipPath, byte[] bytes) {
        items.add(new ZipBuilderItem(zipPath, bytes));
        return this;
    }

    public ZipBuilder put(String zipPath, InputStream inputStream) {
        items.add(new ZipBuilderItem(zipPath, inputStream));
        return this;
    }

    public ZipBuilder remove(String zipPath) {
        var p = URIUtils.normalize(zipPath);
        if (ObjectUtils.notBlank(zipPath)) {
            items.removeIf(c -> c.zipPath.startsWith(p));
        }
        return this;
    }

    public ZipBuilderItem get(String zipPath) {
        var p = URIUtils.normalize(zipPath);
        if (ObjectUtils.notBlank(zipPath)) {
            return items.stream().filter(c -> c.zipPath.startsWith(p)).findAny().orElse(null);
        }
        return null;
    }

    public List<ZipBuilderItem> items() {
        return items;
    }

    public void writeToZipOutputStream(ZipOutputStream zos) throws IOException {
        for (var i : items) {
            i.writeToZipOutputStream(zos);
        }
    }

    public byte[] toBytes(ZipOptions zipOptions) throws Exception {
        var bo = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(bo, zipOptions.charset())) {
            zos.setComment(zipOptions.comment());
            zos.setLevel(zipOptions.level());
            //遍历目录
            writeToZipOutputStream(zos);
        }
        return bo.toByteArray();
    }

    /// 将 virtualFile 转换为 byte 数组 方便前台用户下载使用
    ///
    /// @return a
    /// @throws java.lang.Exception a
    public byte[] toBytes() throws Exception {
        return toBytes(new ZipOptions());
    }

    /// 将一个虚拟文件压缩
    ///
    /// @param outputPath a
    /// @return a
    /// @throws java.io.IOException if any.
    public Path toFile(Path outputPath, ZipOptions zipOptions) throws IOException {
        // 创建一个新的空的输出文件的临时文件
        Files.createDirectories(outputPath.getParent());
        try (var zos = new ZipOutputStream(Files.newOutputStream(outputPath), zipOptions.charset())) {
            zos.setComment(zipOptions.comment());
            zos.setLevel(zipOptions.level());
            writeToZipOutputStream(zos);
        }
        return outputPath;
    }

    public Path toFile(Path outputPath) throws IOException {
        return toFile(outputPath, new ZipOptions());
    }

}
