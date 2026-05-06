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

    private final List<Class<?>> candidates = new ArrayList<>();

    private final List<Predicate<Class<?>>> componentSelectors = new ArrayList<>();

    private final List<Object> componentInstances = new ArrayList<>();

    public static ScxAppModuleDefinition empty() {
        return new ScxAppModuleDefinition();
    }

    public List<Class<?>> candidates() {
        return candidates;
    }

    public ScxAppModuleDefinition candidates(Collection<Class<?>> candidates) {
        this.candidates.addAll(candidates);
        return this;
    }

    public ScxAppModuleDefinition candidate(Class<?>... candidates) {
        this.candidates.addAll(List.of(candidates));
        return this;
    }

    public List<Predicate<Class<?>>> componentSelectors() {
        return componentSelectors;
    }

    public ScxAppModuleDefinition componentSelector(Predicate<Class<?>> componentSelector) {
        this.componentSelectors.add(componentSelector);
        return this;
    }

    public ScxAppModuleDefinition componentSelectors(Collection<Predicate<Class<?>>> componentSelectors) {
        this.componentSelectors.addAll(componentSelectors);
        return this;
    }

    public List<Object> componentInstances() {
        return componentInstances;
    }

    public <T> ScxAppModuleDefinition componentInstance(T component) {
        this.componentInstances.add( component);
        return this;
    }

}
