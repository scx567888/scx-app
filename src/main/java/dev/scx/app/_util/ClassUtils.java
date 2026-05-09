package dev.scx.app._util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static dev.scx.string.ScxString.endsWithIgnoreCase;

/// 类工具类
///
/// 如需高级功能 请使用 scx-reflect 模块
///
/// @author scx567888
/// @version 0.0.1
public final class ClassUtils {

    /// 默认 classLoader
    private static final ClassLoader DEFAULT_CLASS_LOADER = ClassUtils.class.getClassLoader();


    /// 根据 class 获取源地址
    ///
    /// @param source a [java.lang.Class] object.
    /// @return 可能是 目录 也可能是 jar 文件
    public static URI getCodeSource(Class<?> source) {
        return URI.create(source.getProtectionDomain().getCodeSource().getLocation().toString());
    }



    /// 判断路径是否是一个 jar 文件 (这里只是简单的使用 文件后缀判断,并不准确)
    public static boolean isJar(Path path) {
        return Files.isRegularFile(path) && endsWithIgnoreCase(path.toString(), ".jar");
    }

    /// 根据 basePackage 对 class 进行过滤
    public static Class<?>[] filterByBasePackage(Class<?>[] classList, String basePackageName) {
        var p = basePackageName + ".";
        return Arrays.stream(classList).filter(c -> c.getPackageName().equals(basePackageName) || c.getPackageName().startsWith(p)).toArray(Class[]::new);
    }

    /// 从 JarEntry 加载 class
    private static Class<?> loadClassFromJar(JarEntry jarEntry, ClassLoader jarClassLoader) {
        var suffixLength = ".class".length();
        //这里是可以保证 path 最后一定是 .class 所以在此处可以放心移除
        var className = jarEntry.getName().substring(0, jarEntry.getName().length() - suffixLength).replace('/', '.');
        return loadClass0(className, jarClassLoader);
    }

    /// 从 Path 加载 class
    private static Class<?> loadClassFromPath(Path classRealPath, ClassLoader classLoader) {
        var suffixLength = ".class.".length();
        var str = new StringBuilder();
        for (var path : classRealPath) {
            str.append(path.toString()).append(".");
        }
        //这里会在最后添加一个多余的 . 在这里移除
        var className = str.substring(0, str.length() - suffixLength);
        return loadClass0(className, classLoader);
    }

    /// 加载 class 先使用 默认的 classloader 失败后使用 备选的 classloader
    private static Class<?> loadClass0(String className, ClassLoader secondClassLoader) {
        try {
            return DEFAULT_CLASS_LOADER.loadClass(className);
        } catch (ClassNotFoundException t1) {
            try {
                return secondClassLoader.loadClass(className);
            } catch (ClassNotFoundException t2) {
                return null;
            }
        }
    }

    /// 读取 jar 包中的所有 class
    public static Class<?>[] findClassListFromJar(URI jarFileURI) throws IOException {
        try (var jarFile = new JarFile(new File(jarFileURI)); var jarClassLoader = new URLClassLoader(new URL[]{jarFileURI.toURL()}); var jarEntryStream = jarFile.stream()) {
            return jarEntryStream.filter(jarEntry -> !jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")).map(jarEntry -> loadClassFromJar(jarEntry, jarClassLoader)).toArray(Class[]::new);
        }
    }

    /// 根据文件获取 class 列表
    public static Class<?>[] findClassListFromPath(Path classRootPath, ClassLoader classLoader) throws IOException {
        try (var pathStream = Files.walk(classRootPath)) {
            return pathStream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".class")).map(path -> loadClassFromPath(classRootPath.relativize(path), classLoader)).toArray(Class[]::new);
        }
    }

}
