package dev.scx.app;

import dev.scx.ansi.Ansi;
import dev.scx.app.config.ScxConfig;
import dev.scx.app.config.ScxEnvironment;
import dev.scx.di.ComponentContainer;
import dev.scx.di.DefaultComponentContainer;
import dev.scx.di.dependency_resolver.InjectAnnotationDependencyResolver;
import dev.scx.di.dependency_resolver.ValueAnnotationDependencyResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public final class ScxApp {

    private final List<ScxAppModule> appModules;

    private final ComponentContainer componentContainer;

    private List<ScxAppModule> sortedAppModules;

    private final List<ScxAppModule> startedAppModules = new ArrayList<>();

    private final ScxEnvironment scxEnvironment;
    private final ScxConfig scxConfig;

    public ScxApp(ScxEnvironment scxEnvironment, ScxConfig scxConfig, ScxAppModule[] appModules) {
        this.scxEnvironment=scxEnvironment;
        this.scxConfig=scxConfig;
        this.appModules = List.of(appModules);
        this.componentContainer = new DefaultComponentContainer();
        this.sortedAppModules = List.of();
    }

    public static ScxAppBuilder builder() {
        return new ScxAppBuilder();
    }

    public void run() {
        var initContext = new ScxAppInitContext(scxConfig,scxEnvironment);
        var definitions = new ArrayList<ScxAppModuleDefinition>();

        // 1. 调用所有模块 init, 收集 definition
        for (var appModule : appModules) {
            componentContainer.registerComponent(appModule.getClass().getName(), appModule);

            var definition = appModule.init(initContext);
            definitions.add(definition);
        }

        // todo 这里应该注入 一些有用的 变量 从 definitions 中获取? 还是直接使用 config ?
        componentContainer.dependencyResolvers().add(new ValueAnnotationDependencyResolver((k, t)-> null));
        //这里添加一个 bean 的后置处理器以便可以使用 @Autowired 注解
        componentContainer.dependencyResolvers().add(new InjectAnnotationDependencyResolver(componentContainer));

        // 2. 根据 definition 计算 start 顺序
        this.sortedAppModules = ScxAppModuleStartOrderResolver.resolve(appModules, definitions);

        // 3. 汇总组件定义
        var allCandidates = new HashSet<Class<?>>();
        var allComponentSelectors = new ArrayList<Predicate<Class<?>>>();
        var allComponentInstances = new ArrayList<>();

        for (var definition : definitions) {
            allCandidates.addAll(definition.candidates());
            allComponentSelectors.addAll(definition.componentSelectors());
            allComponentInstances.addAll(definition.componentInstances());
        }

        // 4. 根据所有 selector 从所有 candidates 中筛选组件类
        var allComponentClass = new ArrayList<Class<?>>();

        for (var candidate : allCandidates) {
            for (var componentSelector : allComponentSelectors) {
                if (componentSelector.test(candidate)) {
                    allComponentClass.add(candidate);
                    break;
                }
            }
        }

        // 5. 注册组件类
        for (var componentClass : allComponentClass) {
            componentContainer.registerComponentClass(componentClass.getName(), componentClass);
        }

        // 6. 注册组件实例
        for (var componentInstance : allComponentInstances) {
            componentContainer.registerComponent(componentInstance.getClass().getName(), componentInstance);
        }

        // 7. 初始化 DI
        componentContainer.initializeComponents();

        // 8. 按排序启动模块
        try {
            for (var appModule : sortedAppModules) {
                appModule.start(this);
                startedAppModules.add(appModule);
            }
        } catch (Throwable e) {
            stopStartedModules(e);
            throw e;
        }


        addShutdownHook();

    }

    public void stop() {
        stopStartedModules(null);
    }

    private void stopStartedModules(Throwable cause) {
        for (var i = startedAppModules.size() - 1; i >= 0; i = i - 1) {
            var appModule = startedAppModules.get(i);

            try {
                appModule.stop(this);
            } catch (Throwable stopError) {
                if (cause != null) {
                    cause.addSuppressed(stopError);
                } else {
                    throw stopError;
                }
            }
        }

        startedAppModules.clear();
    }

    public ComponentContainer componentContainer() {
        return componentContainer;
    }

    public ScxConfig scxConfig() {
        return scxConfig;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.stop();
            Ansi.ansi().red("项目正在停止!!!").println();
        }));
    }

    public <T> T getComponent(Class<T> requiredType) {
        return componentContainer.getComponent(requiredType);
    }

    public ScxEnvironment scxEnvironment() {
        return scxEnvironment;
    }
}
