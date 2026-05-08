package dev.scx.app;

import dev.scx.app.component.Component;
import dev.scx.app.base.BaseModel;
import dev.scx.app.base.BaseModelService;
import dev.scx.app.util.ClassUtils;
import dev.scx.app._old.config.handler.ConvertValueHandler;
import dev.scx.app._old.config.handler.DefaultValueHandler;
import dev.scx.di.ComponentContainer;
import dev.scx.di.DefaultComponentContainer;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.dependency_resolver.ValueAnnotationDependencyResolver;
import dev.scx.di.exception.DuplicateComponentNameException;
import dev.scx.di.exception.IllegalComponentClassException;
import dev.scx.di.exception.NoSuchConstructorException;
import dev.scx.di.exception.NoUniqueConstructorException;
import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.web.annotation.Routes;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    static List<Class<?>> findClassListByScxModule(Class<? extends ScxAppModule> c) throws IOException {
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
