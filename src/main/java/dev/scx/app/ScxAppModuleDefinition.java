package dev.scx.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/// ScxAppModule 的定义信息.
///
/// 模块通过此对象声明自己向 ScxApp 贡献的内容.
/// 该对象只用于 ScxApp 构建阶段, 不表示运行时资源.
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

    public List<Class<?>> candidates() {
        return candidates;
    }

    public ScxAppModuleDefinition candidate(Class<?>... candidates) {
        this.candidates.addAll(List.of(candidates));
        return this;
    }

    public List<Predicate<Class<?>>> componentSelectors() {
        return componentSelectors;
    }

    public ScxAppModuleDefinition componentSelector(Predicate<Class<?>>... componentSelector) {
        this.componentSelectors.addAll(List.of(componentSelector));
        return this;
    }

    public List<Object> componentInstances() {
        return componentInstances;
    }

    public ScxAppModuleDefinition componentInstance(Object... component) {
        this.componentInstances.addAll(List.of(component));
        return this;
    }

    public List<Class<? extends ScxAppModule>> startBefores() {
        return startBefores;
    }

    public ScxAppModuleDefinition startBefore(Class<? extends ScxAppModule>... startBefores) {
        this.startBefores.addAll(List.of(startBefores));
        return this;
    }

    public List<Class<? extends ScxAppModule>> startAfters() {
        return startAfters;
    }

    public ScxAppModuleDefinition startAfter(Class<? extends ScxAppModule>... startAfters) {
        this.startAfters.addAll(List.of(startAfters));
        return this;
    }

}
