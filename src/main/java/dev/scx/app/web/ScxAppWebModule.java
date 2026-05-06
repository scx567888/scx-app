package dev.scx.app.web;

import dev.scx.app.ScxAppDefineContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;
import dev.scx.app._old.ScxApp;
import dev.scx.http.routing.Router;
import dev.scx.http.routing.x.static_files.StaticFilesHandler;
import dev.scx.web.annotation.Routes;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;

public class ScxAppWebModule implements ScxAppModule {

    @Override
    public ScxAppModuleDefinition define(ScxAppDefineContext context) {
        return new ScxAppModuleDefinition()
            .componentSelector(c ->
                c.getAnnotation(Routes.class) != null
            );
    }

    @Override
    public void init(ScxApp scx) {

    }

}
