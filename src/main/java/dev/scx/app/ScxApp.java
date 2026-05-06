package dev.scx.app;

import dev.scx.di.ComponentContainer;
import dev.scx.di.DefaultComponentContainer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public class ScxApp {

    private final List<ScxAppModule> appModules;
    private final ComponentContainer componentContainer;

    public ScxApp(ScxAppModule[] appModules) {
        this.appModules = List.of(appModules);
        this.componentContainer = new DefaultComponentContainer();
    }

    public static ScxAppBuilder builder() {
        return new ScxAppBuilder();
    }

    public void run() {
        var defineContext = new ScxAppDefineContext();
        var definitions = new ArrayList<ScxAppModuleDefinition>();

        for (var appModule : appModules) {
            // 模块自身也注入到 DI 中.
            componentContainer.registerComponent(appModule.getClass().getName(),appModule);
            // 收集模块定义
            var definition = appModule.define(defineContext);
            definitions.add(definition);
        }

        var allCandidates = new HashSet<Class<?>>();
        var allComponentSelectors = new ArrayList<Predicate<Class<?>>>();
        var allComponentInstances = new ArrayList<>();

        for (var definition : definitions) {
            allCandidates.addAll(definition.candidates());
            allComponentSelectors.addAll(definition.componentSelectors());
            allComponentInstances.addAll(definition.componentInstances());
        }

        var allComponentClass = new ArrayList<Class<?>>();

        for (var candidate : allCandidates) {

            // 任意一个 选择器 为 true 就认为是一个 ComponentClass
            selector:
            for (var componentSelector : allComponentSelectors) {
                if (componentSelector.test(candidate)) {
                    allComponentClass.add(candidate);
                    break selector;
                }
            }

        }

        for (var componentClass : allComponentClass) {
            componentContainer.registerComponentClass(componentClass.getName(), componentClass);
        }

        for (var componentInstance : allComponentInstances) {
            componentContainer.registerComponent(componentInstance.getClass().getName(), componentInstance);
        }

        // 验证 DI 是否有问题
        componentContainer.initializeComponents();


        // 初始化所有模块
        for (var appModule : appModules) {
            appModule.init(this);
        }

        // 启动所有模块
        for (var appModule : appModules) {
            appModule.start(this);
        }

    }

}
