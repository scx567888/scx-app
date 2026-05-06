package dev.scx.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScxAppBuilder {

    private List<ScxAppModule> appModules;

    public ScxAppBuilder() {
        this.appModules = new ArrayList<>();
    }

    public ScxAppBuilder module(ScxAppModule... appModules) {
        Collections.addAll(this.appModules, appModules);
        return this;
    }

    public ScxApp build() {
        return new ScxApp(appModules.toArray(ScxAppModule[]::new));
    }


}
