package dev.scx.app;

import dev.scx.app.component.Component;
import dev.scx.app.base.BaseModel;
import dev.scx.app.base.BaseModelService;
import dev.scx.app.util.ClassUtils;
import dev.scx.app._old.config.ScxFeatureConfig;
import dev.scx.app._old.config.handler.AppRootHandler;
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

import static dev.scx.app._old.enumeration.ScxAppFeature.USE_SPY;
import static java.util.Objects.requireNonNull;

/// ScxHelper
///
/// @author scx567888
/// @version 0.0.1
public final class ScxAppHelper {

    /// Constant <code>beanFilterAnnotation</code>
    private static final List<Class<? extends Annotation>> beanFilterAnnotation = List.of(
        //scx 注解
         Component.class, Routes.class);

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

    /// 拥有 scx 注解
    ///
    /// @param clazz class
    /// @return b
    public static boolean isBeanClass(Class<?> clazz) {
        for (var a : beanFilterAnnotation) {
            if (clazz.getAnnotation(a) != null) {
                return true;
            }
        }
        return false;
    }



    public static boolean isScxBaseModelServiceClass(Class<?> c) {
        return c.isAnnotationPresent(Component.class) &&  // 拥有注解
            ClassUtils.isNormalClass(c) && // 是一个普通的类 (不是接口, 不是抽象类) ; 此处不要求有必须有无参构造函数 因为此类的创建会由 beanFactory 进行处理
            c.getGenericSuperclass() instanceof ParameterizedType t && //需要有泛型参数
            t.getActualTypeArguments().length == 1; //并且泛型参数的数量必须是一个
    }

    @SuppressWarnings("unchecked")
    public static <Entity extends BaseModel> Class<Entity> findBaseModelServiceEntityClass(Class<?> baseModelServiceClass) {
        // todo 这里强转可能有问题
        var superClass = ((ClassInfo) ScxReflect.typeOf(baseModelServiceClass)).findSuperType(BaseModelService.class);
        if (superClass != null) {
            var boundType = superClass.bindings().get(0);
            if (boundType != null) {
                return (Class<Entity>) boundType.rawClass();
            } else {
                throw new IllegalArgumentException(baseModelServiceClass.getName() + " : 必须设置泛型参数 !!!");
            }
        } else {
            throw new IllegalArgumentException(baseModelServiceClass.getName() + " : 必须继承自 BaseModelService !!!");
        }
    }

    /// 获取新的可用的端口号 (使用弹窗让用户进行选择)
    ///
    /// @param port a
    /// @return a
    static boolean isUseNewPort(int port) {
        while (true) {
            var errMessage = """
                *******************************************************
                *                                                     *
                *         端口号 [ %s ] 已被占用, 是否采用新端口号 ?       *
                *                                                     *
                *                [Y]es    |    [N]o                   *
                *                                                     *
                *******************************************************
                """;
            System.err.printf((errMessage) + System.lineSeparator(), port);
            var result = System.console().readLine().trim();
            if ("Y".equalsIgnoreCase(result)) {
                return true;
            } else if ("N".equalsIgnoreCase(result)) {
                var ignoreMessage = """
                    *******************************************
                    *                                         *
                    *     N 端口号被占用!!! 服务器启动失败 !!!      *
                    *                                         *
                    *******************************************
                    """;
                System.err.println(ignoreMessage);
                System.exit(-1);
                return false;
            }
        }
    }



    static ScxAppModule[] initScxModuleMetadataList(ScxAppModule[] scxModules) {
        //2, 检查模块参数是否正确
        if (scxModules == null || Arrays.stream(scxModules).noneMatch(Objects::nonNull)) {
            throw new IllegalArgumentException("Modules must not be empty !!!");
        }
        return scxModules;
    }

    static ComponentContainer initBeanFactory(ScxAppModule[] modules, ScxFeatureConfig scxFeatureConfig) throws NoSuchConstructorException, DuplicateComponentNameException, NoUniqueConstructorException, IllegalComponentClassException {
        var beanFactory = new DefaultComponentContainer();
        // todo 这里应该注入 一些有用的 Map 变量
        beanFactory.dependencyResolvers().add(new ValueAnnotationDependencyResolver((k,t)-> null));
        //这里添加一个 bean 的后置处理器以便可以使用 @Autowired 注解
        beanFactory.dependencyResolvers().add(new InjectAnnotationDependencyResolver(beanFactory));
        //注册 bean
        var beanClass = Arrays.stream(modules)
            .flatMap(c -> c.classList().stream())
            .filter(ScxAppHelper::isBeanClass)
            .toArray(Class<?>[]::new);

        for (var c : beanClass) {
            beanFactory.registerComponentClass(c.getName(), c);
        }
        return beanFactory;
    }

    /// 数据源连接异常
    ///
    /// @param e a [java.lang.Exception] object.
    static void dataSourceExceptionHandler(Exception e) {
        while (true) {
            var errMessage = """
                **************************************************************
                *                                                            *
                *           X 数据源连接失败 !!! 是否忽略错误并继续运行 ?            *
                *                                                            *
                *        [Y] 忽略错误并继续运行    |     [N] 退出程序              *
                *                                                            *
                **************************************************************
                """;
            System.err.println(errMessage);
            var result = System.console().readLine().trim();
            if ("Y".equalsIgnoreCase(result)) {
                var ignoreMessage = """
                    *******************************************
                    *                                         *
                    *       N 数据源链接错误,用户已忽略 !!!         *
                    *                                         *
                    *******************************************
                    """;
                System.err.println(ignoreMessage);
                break;
            } else if ("N".equalsIgnoreCase(result)) {
                e.printStackTrace();
                System.exit(-1);
                break;
            }
        }
    }



}
