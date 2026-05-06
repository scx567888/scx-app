package dev.scx.app;

import dev.scx.app._old.ScxApp;

// todo ?
public interface ScxAppModule {

    default ScxAppModuleDefinition define(ScxAppDefineContext context) {
        return ScxAppModuleDefinition.empty();
    }

    default void start(ScxApp scxApp) {

    }

    default void stop(ScxApp scxApp) {

    }

}
