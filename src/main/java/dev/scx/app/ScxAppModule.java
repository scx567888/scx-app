package dev.scx.app;

// todo ?
public interface ScxAppModule {

    default ScxAppModuleDefinition define(ScxAppDefineContext context) {
        return ScxAppModuleDefinition.empty();
    }

    default void init(ScxApp scxApp){

    }

    default void start(ScxApp scxApp) {

    }

    default void stop(ScxApp scxApp) {

    }

}
