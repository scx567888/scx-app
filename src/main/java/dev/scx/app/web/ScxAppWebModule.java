package dev.scx.app.web;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppDefineContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;
import dev.scx.web.annotation.Routes;


public class ScxAppWebModule implements ScxAppModule {

    @Override
    public ScxAppModuleDefinition define(ScxAppDefineContext context) {
        return new ScxAppModuleDefinition()
            .componentSelector(c ->
                c.getAnnotation(Routes.class) != null
            );
    }

    @Override
    public void init(ScxApp scxApp) {

    }

}
