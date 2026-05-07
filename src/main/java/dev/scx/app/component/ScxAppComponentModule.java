package dev.scx.app.component;

import dev.scx.app.ScxAppDefineContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;

public class ScxAppComponentModule implements ScxAppModule {

    @Override
    public ScxAppModuleDefinition define(ScxAppDefineContext context) {
        return new ScxAppModuleDefinition()
            .componentSelector(c ->
                c.getAnnotation(Component.class) != null
            );
    }

}
