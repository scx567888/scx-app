package dev.scx.app.config;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/// 项目环境
///
/// @author scx567888
/// @version 0.0.1
public final class ScxEnvironment {

    /// 项目根模块 所在路径
    /// 默认取 所有自定义模块的最后一个 所在的文件根目录
    private final Path appRootPath;

    /// 临时目录路径
    private final Path tempPath;

    /// 根据 class 推断 class 根目录
    ///
    /// @param mainClass class
    public ScxEnvironment(Class<?> mainClass) {
        this.appRootPath = initAppRoot(mainClass);
        this.tempPath = getPathByAppRoot("AppRoot:_temp");
    }

    /// 根据 mainClass 初始化 项目根目录
    ///
    /// @param mainClass m
    /// @return f
    private static Path initAppRoot(Class<?> mainClass) {
        return getAppRoot(mainClass);
    }

    /// 根据 class 获取源地址
    ///
    /// @param source a [java.lang.Class] object.
    /// @return 可能是 目录 也可能是 jar 文件
    public static URI getCodeSource(Class<?> source) {
        return URI.create(source.getProtectionDomain().getCodeSource().getLocation().toString());
    }

    /// 根据 codeSource 获取 app 根路径(文件夹)
    ///
    /// @param codeSource 参考 getCodeSource(Class)
    /// @return app 根路径(文件夹)
    public static Path getAppRoot(URI codeSource) {
        var path = Path.of(codeSource);
        return Files.isDirectory(path) ? path : path.getParent();
    }

    /// 根据 class 获取 app 根路径(文件夹)
    ///
    /// @param source s
    /// @return app 根路径(文件夹)
    public static Path getAppRoot(Class<?> source) {
        return getAppRoot(getCodeSource(source));
    }

    public Path getPathByAppRoot(String path) {
        if (path.startsWith("AppRoot:")) {
            return Path.of(this.appRootPath.toString(), path.substring("AppRoot:".length()));
        } else {
            return Path.of(path);
        }
    }

    /// 获取临时路径
    ///
    /// @return a
    public Path getTempPath() {
        return tempPath;
    }

    /// 获取临时路径
    ///
    /// @param paths a
    /// @return a
    public Path getTempPath(String... paths) {
        return Path.of(this.tempPath.toString(), paths);
    }

    public Path appRootPath() {
        return appRootPath;
    }

}
