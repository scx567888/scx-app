package dev.scx.app;

import dev.scx.app.annotation.Scheduled;
import dev.scx.app.annotation.ScheduledList;
import dev.scx.app.annotation.ScxService;
import dev.scx.app.base.BaseModel;
import dev.scx.app.base.BaseModelService;
import dev.scx.app.util.ClassUtils;
import dev.scx.app.util.ObjectUtils;
import dev.scx.app.config.ScxConfig;
import dev.scx.app.config.ScxEnvironment;
import dev.scx.app.config.ScxFeatureConfig;
import dev.scx.app.config.handler.AppRootHandler;
import dev.scx.app.config.handler.ConvertValueHandler;
import dev.scx.app.config.handler.DefaultValueHandler;
import dev.scx.data.sql.annotation.Table;
import dev.scx.di.ComponentContainer;
import dev.scx.di.DefaultComponentContainer;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.dependency_resolver.ValueAnnotationDependencyResolver;
import dev.scx.di.exception.DuplicateComponentNameException;
import dev.scx.di.exception.IllegalComponentClassException;
import dev.scx.di.exception.NoSuchConstructorException;
import dev.scx.di.exception.NoUniqueConstructorException;
import dev.scx.jdbc.spy.ScxJdbcSpy;
import dev.scx.jdbc.spy.listener.logging.LoggingDataSourceListener;
import dev.scx.jdbc.spy.listener.logging.PreparedStatementLogStyle;
import dev.scx.logging.ScxLoggerConfig;
import dev.scx.logging.ScxLogging;
import dev.scx.logging.recorder.ConsoleRecorder;
import dev.scx.logging.recorder.FileRecorder;
import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.reflect.TypeReference;
import dev.scx.scheduling.ScxScheduling;
import dev.scx.serialize.ScxSerialize;
import dev.scx.sql.JDBCConnectionInfo;
import dev.scx.sql.SQLClient;
import dev.scx.sql.TypeSQLResolver;
import dev.scx.app.sql.handler.ObjectSQLHandlerFactory;
import dev.scx.web.annotation.Routes;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static dev.scx.app.enumeration.ScxAppFeature.USE_SPY;
import static dev.scx.app.util.ClassUtils.*;
import static dev.scx.reflect.AccessModifier.PUBLIC;
import static java.lang.System.Logger.Level.*;
import static java.util.Objects.requireNonNull;

/// ScxHelper
///
/// @author scx567888
/// @version 0.0.1
public final class ScxAppHelper {

    /// Constant <code>beanFilterAnnotation</code>
    private static final List<Class<? extends Annotation>> beanFilterAnnotation = List.of(
        //scx 注解
         ScxService.class, Routes.class);

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

    /// 初始化 ScxModelClassList
    ///
    /// @param c a
    /// @return a
    public static boolean isScxBaseModelClass(Class<?> c) {
        return c.isAnnotationPresent(Table.class) &&  // 拥有注解
            ClassUtils.isInstantiableClass(c) &&  // 是一个可以不需要其他参数直接生成实例化的对象
            BaseModel.class.isAssignableFrom(c);
    }

    public static boolean isScxBaseModelServiceClass(Class<?> c) {
        return c.isAnnotationPresent(ScxService.class) &&  // 拥有注解
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

    static SQLClient initSQLClient(ScxAppOptions scxOptions, ScxFeatureConfig scxFeatureConfig) {

        return SQLClient.of(
            new JDBCConnectionInfo(
                scxOptions.dataSourceUrl(),
                scxOptions.dataSourceUsername(),
                scxOptions.dataSourcePassword(),
                scxOptions.dataSourceParameters()
                ),
                TypeSQLResolver.registerDefaultHandlers(TypeSQLResolver.builder())
                    .registerHandlerFactory(new ObjectSQLHandlerFactory())
                    .build(),
                    d-> {
                        return scxFeatureConfig.get(USE_SPY) ? ScxJdbcSpy.spy(d, new LoggingDataSourceListener(PreparedStatementLogStyle.RENDERED_SQL)): d;
                    }
            );
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

    public static void startAnnotationScheduled(ComponentContainer beanFactory) {
        var beanDefinitionNames = beanFactory.getComponentNames();
        for (var beanDefinitionName : beanDefinitionNames) {
            var bean = beanFactory.getComponent(beanDefinitionName);
            // todo 这里强转可能有问题
            var classInfo = (ClassInfo) ScxReflect.typeOf(bean.getClass());
            for (var method : classInfo.methods()) {
                if (method.accessModifier() != PUBLIC) {
                    continue;
                }
                var scheduledList = Arrays.stream(method.annotations()).flatMap(c -> switch (c) {
                    case Scheduled s -> Stream.of(s);
                    case ScheduledList f -> Stream.of(f.value());
                    default -> Stream.of();
                }).toList();
                for (Scheduled scheduled : scheduledList) {
                    if (method.parameters().length != 0) {
                        ScxApp.logger.log(ERROR,
                            "被 Scheduled 注解标识的方法不可以有参数 Class [{0}] , Method [{1}]",
                            classInfo.name(),
                            method.name()
                        );
                        break;
                    }
                    if (method.isStatic()) {
                        ScxScheduling.cron()
                            .cronExpression(scheduled.cron())
                            .start(c -> {
                                try {
                                    method.invoke(null);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    } else {
                        ScxScheduling.cron()
                            .cronExpression(scheduled.cron())
                            .start(c -> {
                                try {
                                    method.invoke(bean);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    }
                }

            }
        }
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

    static void initScxLoggerFactory(ScxConfig scxConfig, ScxEnvironment scxEnvironment) {
        initScxLoggerFactory0(scxConfig, scxEnvironment);
        scxConfig.onChange((oldValue, newValue) -> {
            initScxLoggerFactory0(scxConfig, scxEnvironment);
        });
    }

    /// a
    ///
    /// @param scxConfig      a
    /// @param scxEnvironment a
    static void initScxLoggerFactory0(ScxConfig scxConfig, ScxEnvironment scxEnvironment) {
        //先初始化好 DefaultScxLoggerInfo
        var defaultLevel = toLevel(scxConfig.get("scx.logging.default.level", String.class));
        var defaultType = toType(scxConfig.get("scx.logging.default.type", String.class));
        var defaultStoredDirectory = scxConfig.get("scx.logging.default.stored-directory", AppRootHandler.of(scxEnvironment, "AppRoot:logs"));
        var defaultStackTrace = scxConfig.get("scx.logging.default.stack-trace", DefaultValueHandler.of(false));

        //设置默认的 config 这里我们先清除所有的 Recorders
        var defaultConfig = ScxLogging.rootConfig().clearRecorders();
        defaultConfig.setLevel(defaultLevel);
        if (defaultType == LoggingType.CONSOLE || defaultType == LoggingType.BOTH) {
            defaultConfig.addRecorder(new ConsoleRecorder());
        }
        if (defaultType == LoggingType.FILE || defaultType == LoggingType.BOTH) {
            defaultConfig.addRecorder(new FileRecorder(defaultStoredDirectory));
        }
        defaultConfig.setStackTrace(defaultStackTrace);

        //以下日志若有缺少的 storedDirectory 则全部以 defaultStoredDirectory 为准
        var loggers = scxConfig.get("scx.logging.loggers", ConvertValueHandler.of(new TypeReference<List<Map<String, String>>>() {
        }));
        if (loggers != null) {
            for (var logger : loggers) {
                var name = logger.get("name");
                if (ObjectUtils.notBlank(name)) {
                    var level = toLevel(logger.get("level"));
                    var type = toType(logger.get("type"));
                    var storedDirectory = ObjectUtils.notBlank(logger.get("stored-directory")) ? scxEnvironment.getPathByAppRoot(logger.get("stored-directory")) : null;
                    var stackTrace = ScxSerialize.convertObject(logger.get("stack-trace"), Boolean.class);
                    var config = new ScxLoggerConfig();
                    config.setLevel(level);
                    if (type == LoggingType.CONSOLE || type == LoggingType.BOTH) {
                        config.addRecorder(new ConsoleRecorder());
                    }
                    if (type == LoggingType.FILE || type == LoggingType.BOTH) {
                        //文件路径的缺省值使用 默认的
                        config.addRecorder(new FileRecorder(storedDirectory != null ? storedDirectory : defaultStoredDirectory));
                    }
                    config.setStackTrace(stackTrace);
                    ScxLogging.setConfig(name, config);
                }
            }
        }
    }

    private static System.Logger.Level toLevel(String levelName) {
        Objects.requireNonNull(levelName, "levelName 不能为空 !!!");
        var s = levelName.trim().toUpperCase();
        return switch (s) {
            case "OFF", "O" -> OFF;
            case "ERROR", "E" -> ERROR;
            case "WARN", "WARNING", "W" -> WARNING;
            case "INFO", "I" -> INFO;
            case "DEBUG", "D" -> DEBUG;
            case "TRACE", "T" -> TRACE;
            case "ALL", "A" -> ALL;
            default -> null;
        };
    }

    private static LoggingType toType(String loggingTypeName) {
        requireNonNull(loggingTypeName, "loggingTypeName 不能为空 !!!");
        var s = loggingTypeName.trim().toUpperCase();
        return switch (s) {
            case "CONSOLE", "C" -> LoggingType.CONSOLE;
            case "FILE", "F" -> LoggingType.FILE;
            case "BOTH", "B" -> LoggingType.BOTH;
            default -> null;
        };
    }

    private enum LoggingType {

        /// 打印到控制台
        CONSOLE,

        /// 写入到文件
        FILE,

        /// 既打印到控制台也同时写入到文件
        BOTH
    }

}
