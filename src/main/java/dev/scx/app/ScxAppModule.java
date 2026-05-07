package dev.scx.app;

/// ScxApp 的模块.
///
/// 一个 ScxAppModule 表示 ScxApp 启动流程中的一个动作单元.
///
/// ScxAppModule 的完整执行顺序如下:
///
/// 1. ScxApp 调用所有模块的 `init(ScxAppInitContext)`
///    每个模块根据当前配置和初始化上下文创建自身所需的对象,
///    并返回 ScxAppModuleDefinition.
///
/// 2. ScxApp 汇总所有 ScxAppModuleDefinition
///    ScxApp 会根据所有模块返回的定义:
///    - 收集组件候选类
///    - 收集组件选择器
///    - 收集组件实例
///    - 构建并初始化 DI 容器
///    - 根据 startBefore / startAfter 计算模块 start 顺序
///
/// 3. ScxApp 按计算出的顺序调用所有模块的 start(ScxApp)
///    此时 DI 容器已经完成构建, 模块可以通过 ScxApp 访问应用运行时能力.
///
/// 4. ScxApp 在停止应用时, 按成功 start 的反向顺序调用 stop(ScxApp).
///
/// start 的含义是“执行该模块在启动流程中的动作”.
/// 该动作可以是注册路由、修复数据库表、启动服务器、启动调度器等.
///
/// @author scx567888
/// @version 0.0.1
public interface ScxAppModule {

    /// 初始化当前模块, 并返回当前模块在本次 ScxApp 中的定义.
    ///
    /// 该方法会在 DI 容器构建之前被调用.
    /// ScxApp 会收集所有模块返回的 ScxAppModuleDefinition,
    /// 并在之后统一构建 DI 容器和模块启动顺序.
    ///
    /// @param context 当前 ScxApp 的初始化上下文
    /// @return 当前模块的定义信息
    default ScxAppModuleDefinition init(ScxAppInitContext context) {
        return ScxAppModuleDefinition.of();
    }

    /// 执行当前模块的启动动作.
    ///
    /// 该方法会在所有模块 init 完成, 且 ScxApp 完成 DI 容器构建之后调用.
    /// ScxApp 会根据各模块定义中的 startBefore / startAfter 计算调用顺序.
    ///
    /// @param scxApp 当前 ScxApp 实例
    default void start(ScxApp scxApp) {

    }

    /// 停止当前模块.
    ///
    /// ScxApp 会按成功 start 的反向顺序调用该方法.
    ///
    /// @param scxApp 当前 ScxApp 实例
    default void stop(ScxApp scxApp) {

    }

}
