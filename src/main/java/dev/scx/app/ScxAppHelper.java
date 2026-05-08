package dev.scx.app;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.scx.app.util.ClassUtils.*;

/// ScxHelper
///
/// @author scx567888
/// @version 0.0.1
public final class ScxAppHelper {

    static Path findRootPathByScxModule(Class<? extends ScxAppModule> c) throws IOException {
        var classSource = getCodeSource(c);
        var classSourcePath = Path.of(classSource);
        var isJar = isJar(classSourcePath);
        //判断当前是否处于 jar 包中 并使用不同的 方式加载
        return isJar ? classSourcePath.getParent() : classSourcePath;
    }

    /// 根据 ScxModule 的 class 查找 所有 class
    ///
    /// @param c c
    /// @return class 列表 (注意这里返回的是不可变的列表 !!!)
    /// @throws IOException r
    public static List<Class<?>> findClassListByScxModule(Class<? extends ScxAppModule> c) throws IOException {
        var classSource = getCodeSource(c);
        var classSourcePath = Path.of(classSource);
        var isJar = isJar(classSourcePath);
        //判断当前是否处于 jar 包中 并使用不同的 方式加载
        var allClassList = isJar ? findClassListFromJar(classSource) : findClassListFromPath(classSourcePath, c.getClassLoader());
        //使用 basePackage 过滤
        var basePackage = c.getPackageName();
        return List.of(filterByBasePackage(allClassList, basePackage));
    }

}
