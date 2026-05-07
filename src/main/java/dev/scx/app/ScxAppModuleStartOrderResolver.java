package dev.scx.app;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/// ScxAppModule start 顺序解析器.
///
/// 规则:
///
/// - A.startBefore(B) 表示 A.start 必须早于 B.start.
/// - A.startAfter(B) 表示 A.start 必须晚于 B.start.
/// - startBefore / startAfter 引用的模块必须存在.
/// - 同一个 ScxAppModule 类型在一个 ScxApp 中只能出现一次.
/// - 没有顺序关系的模块, 保持用户注册顺序.
/// - 如果存在循环依赖, 解析失败.
final class ScxAppModuleStartOrderResolver {

    static List<ScxAppModule> resolve(List<ScxAppModule> modules, List<ScxAppModuleDefinition> definitions) {
        if (modules.size() != definitions.size()) {
            throw new IllegalArgumentException("modules 和 definitions 数量不一致 !!!");
        }

        var moduleByClass = new LinkedHashMap<Class<? extends ScxAppModule>, ScxAppModule>();
        var indexByClass = new HashMap<Class<? extends ScxAppModule>, Integer>();

        for (var i = 0; i < modules.size(); i = i + 1) {
            var module = modules.get(i);
            var moduleClass = moduleClass(module);

            var old = moduleByClass.putIfAbsent(moduleClass, module);
            if (old != null) {
                throw new IllegalStateException("重复的 ScxAppModule 类型 : " + moduleClass.getName());
            }

            indexByClass.put(moduleClass, i);
        }

        var graph = new LinkedHashMap<Class<? extends ScxAppModule>, Set<Class<? extends ScxAppModule>>>();
        var indegree = new HashMap<Class<? extends ScxAppModule>, Integer>();

        for (var moduleClass : moduleByClass.keySet()) {
            graph.put(moduleClass, new LinkedHashSet<>());
            indegree.put(moduleClass, 0);
        }

        for (var i = 0; i < modules.size(); i = i + 1) {
            var currentModule = modules.get(i);
            var currentClass = moduleClass(currentModule);
            var definition = definitions.get(i);

            // current.startBefore(target)
            // current -> target
            for (var targetClass : definition.startBefores()) {
                checkTargetExists(currentClass, "startBefore", targetClass, moduleByClass);
                addEdge(graph, indegree, currentClass, targetClass);
            }

            // current.startAfter(target)
            // target -> current
            for (var targetClass : definition.startAfters()) {
                checkTargetExists(currentClass, "startAfter", targetClass, moduleByClass);
                addEdge(graph, indegree, targetClass, currentClass);
            }
        }

        var ready = new PriorityQueue<Class<? extends ScxAppModule>>(
            Comparator.comparingInt(indexByClass::get)
        );

        for (var moduleClass : moduleByClass.keySet()) {
            if (indegree.get(moduleClass) == 0) {
                ready.add(moduleClass);
            }
        }

        var sortedClasses = new ArrayList<Class<? extends ScxAppModule>>();

        while (!ready.isEmpty()) {
            var current = ready.remove();
            sortedClasses.add(current);

            for (var next : graph.get(current)) {
                var newIndegree = indegree.get(next) - 1;
                indegree.put(next, newIndegree);

                if (newIndegree == 0) {
                    ready.add(next);
                }
            }
        }

        if (sortedClasses.size() != modules.size()) {
            var cycleModules = indegree.entrySet()
                .stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> e.getKey().getName())
                .toList();

            throw new IllegalStateException("ScxAppModule start 顺序存在循环依赖 : " + cycleModules);
        }

        var sortedModules = new ArrayList<ScxAppModule>();

        for (var moduleClass : sortedClasses) {
            sortedModules.add(moduleByClass.get(moduleClass));
        }

        return sortedModules;
    }

    private static void addEdge(
        LinkedHashMap<Class<? extends ScxAppModule>, Set<Class<? extends ScxAppModule>>> graph,
        HashMap<Class<? extends ScxAppModule>, Integer> indegree,
        Class<? extends ScxAppModule> from,
        Class<? extends ScxAppModule> to
    ) {
        if (from == to) {
            throw new IllegalStateException("ScxAppModule 不能声明自身顺序关系 : " + from.getName());
        }

        var added = graph.get(from).add(to);

        // 避免重复边导致入度重复增加
        if (added) {
            indegree.put(to, indegree.get(to) + 1);
        }
    }

    private static void checkTargetExists(
        Class<? extends ScxAppModule> currentClass,
        String relation,
        Class<? extends ScxAppModule> targetClass,
        LinkedHashMap<Class<? extends ScxAppModule>, ScxAppModule> moduleByClass
    ) {
        if (!moduleByClass.containsKey(targetClass)) {
            throw new IllegalStateException(
                currentClass.getName() + " 声明了 " + relation + "(" + targetClass.getName() + "), 但该模块不存在 !!!"
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends ScxAppModule> moduleClass(ScxAppModule module) {
        return (Class<? extends ScxAppModule>) module.getClass();
    }

}
