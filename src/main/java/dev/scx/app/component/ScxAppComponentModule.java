package dev.scx.app.component;

import dev.scx.app.ScxAppInitContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;

public final class ScxAppComponentModule implements ScxAppModule {

    @Override
    public ScxAppModuleDefinition init(ScxAppInitContext context) {
        return ScxAppModuleDefinition.of()
            .componentSelector(c ->
                c.getAnnotation(Component.class) != null
            );
    }

}
