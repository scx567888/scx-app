package dev.scx.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/// ScxAppModule 在当前 ScxApp 中的定义信息.
///
/// 每个 ScxAppModule 会在 init(ScxAppInitContext) 阶段返回一个
/// ScxAppModuleDefinition.
///
/// ScxApp 会在所有模块 init 完成后汇总这些定义, 并据此完成:
///
/// - 组件候选类收集
/// - 组件选择器收集
/// - 组件实例收集
/// - DI 容器构建
/// - 模块 start 顺序计算
///
/// startBefore / startAfter 同时表达模块存在关系和 start 顺序关系:
///
/// - startBefore(A) 表示当前模块的 start 在 A.start 之前执行,
///   且 A 必须存在于当前 ScxApp 中.
///
/// - startAfter(A) 表示当前模块的 start 在 A.start 之后执行,
///   且 A 必须存在于当前 ScxApp 中.
///
/// 如果 startBefore / startAfter 形成环, ScxApp 会在构建阶段失败.
///
/// @author scx567888
/// @version 0.0.1
public class ScxAppModuleDefinition {

    private final List<Class<?>> candidates;

    private final List<Predicate<Class<?>>> componentSelectors;

    private final List<Object> componentInstances;

    private final List<Class<? extends ScxAppModule>> startBefores;

    private final List<Class<? extends ScxAppModule>> startAfters;

    private ScxAppModuleDefinition() {
        this.candidates = new ArrayList<>();
        this.componentSelectors = new ArrayList<>();
        this.componentInstances = new ArrayList<>();
        this.startBefores = new ArrayList<>();
        this.startAfters = new ArrayList<>();
    }

    public static ScxAppModuleDefinition of() {
        return new ScxAppModuleDefinition();
    }

    /// 返回当前模块提供的组件候选类.
    public List<Class<?>> candidates() {
        return candidates;
    }

    /// 添加当前模块提供的组件候选类.
    public ScxAppModuleDefinition candidate(Class<?>... candidates) {
        this.candidates.addAll(List.of(candidates));
        return this;
    }

    /// 返回当前模块提供的组件选择器.
    ///
    /// ScxApp 会使用所有模块提供的组件选择器,
    /// 从所有候选类中筛选出需要注册到 DI 容器的组件类.
    public List<Predicate<Class<?>>> componentSelectors() {
        return componentSelectors;
    }

    /// 添加当前模块提供的组件选择器.
    public ScxAppModuleDefinition componentSelector(Predicate<Class<?>>... componentSelector) {
        this.componentSelectors.addAll(List.of(componentSelector));
        return this;
    }

    /// 返回当前模块提供的组件实例.
    ///
    /// ScxApp 会将这些实例注册到 DI 容器.
    public List<Object> componentInstances() {
        return componentInstances;
    }

    /// 添加当前模块提供的组件实例.
    public ScxAppModuleDefinition componentInstance(Object... component) {
        this.componentInstances.addAll(List.of(component));
        return this;
    }

    /// 返回当前模块需要早于哪些模块执行 start.
    public List<Class<? extends ScxAppModule>> startBefores() {
        return startBefores;
    }

    /// 声明当前模块的 start 需要早于指定模块执行.
    ///
    /// 被引用的模块必须存在于当前 ScxApp 中.
    public ScxAppModuleDefinition startBefore(Class<? extends ScxAppModule>... startBefores) {
        this.startBefores.addAll(List.of(startBefores));
        return this;
    }

    /// 返回当前模块需要晚于哪些模块执行 start.
    public List<Class<? extends ScxAppModule>> startAfters() {
        return startAfters;
    }

    /// 声明当前模块的 start 需要晚于指定模块执行.
    ///
    /// 被引用的模块必须存在于当前 ScxApp 中.
    public ScxAppModuleDefinition startAfter(Class<? extends ScxAppModule>... startAfters) {
        this.startAfters.addAll(List.of(startAfters));
        return this;
    }

}
