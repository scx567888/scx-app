package dev.scx.app;

import dev.scx.app.config.ScxConfig;
import dev.scx.app.config.ScxEnvironment;

// todo ?
public class ScxAppInitContext {

    private final ScxConfig scxConfig;
    private final ScxEnvironment scxEnvironment;

    public ScxAppInitContext(ScxConfig scxConfig, ScxEnvironment scxEnvironment) {
        this.scxConfig=scxConfig;
        this.scxEnvironment=scxEnvironment;
    }

    public ScxConfig config(){
        return scxConfig;
    }

    public ScxEnvironment environment(){
        return scxEnvironment;
    }

}
