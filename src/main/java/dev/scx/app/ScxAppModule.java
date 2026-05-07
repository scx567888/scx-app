package dev.scx.app;


public interface ScxAppModule {

    /// 收集 类, 注册 组件选择器
    ///
    /// @return
    default ScxAppModuleDefinition init(ScxAppInitContext context) {
        return ScxAppModuleDefinition.of();
    }

    /// start
    default void start(ScxApp scxApp) {

    }

    /// stop
    default void stop(ScxApp scxApp) {

    }

}
