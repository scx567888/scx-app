package dev.scx.app._util.zip;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.zip.Deflater.DEFAULT_COMPRESSION;

/// ZipOptions
///
/// @author scx567888
/// @version 0.0.1
public final class ZipOptions {

    /// 是否包含根目录
    /// 压缩时使用则会直接压缩整个文件夹, 否则相当于压缩文件内文件夹
    /// 解压时使用则会在待解压目录创建与压缩包名称相同的文件夹
    /// 只针对文件夹生效
    private boolean includeRoot;

    /// 是否使用原始文件名 使用时 zipPath 将会变为文件夹
    private boolean useOriginalFileName;

    /// 字符集
    private Charset charset;

    /// 压缩级别
    private int level;

    /// 注释
    private String comment;

    public ZipOptions() {
        this.includeRoot = false;
        this.useOriginalFileName = false;
        this.charset = StandardCharsets.UTF_8;
        this.level = DEFAULT_COMPRESSION;
    }

    public boolean includeRoot() {
        return includeRoot;
    }

    public boolean useOriginalFileName() {
        return useOriginalFileName;
    }

    public Charset charset() {
        return charset;
    }

    public int level() {
        return level;
    }

    public String comment() {
        return comment;
    }

    public ZipOptions setIncludeRoot(boolean includeRoot) {
        this.includeRoot = includeRoot;
        return this;
    }

    public ZipOptions setUseOriginalFileName(boolean useOriginalFileName) {
        this.useOriginalFileName = useOriginalFileName;
        return this;
    }

    public ZipOptions setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public ZipOptions setLevel(int level) {
        this.level = level;
        return this;
    }

    public ZipOptions setComment(String comment) {
        this.comment = comment;
        return this;
    }

}
