package dev.scx.app.web;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppInitContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;
import dev.scx.app.config.ConfiguredPath;
import dev.scx.app.http.ScxAppHttpModule;
import dev.scx.di.provider.ComponentProvider;
import dev.scx.http.routing.Router;
import dev.scx.web.ScxWeb;
import dev.scx.web.ScxWebRoute;
import dev.scx.web.annotation.Routes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ScxAppWebModule implements ScxAppModule {

    private ScxWeb scxWeb;

    @Override
    public ScxAppModuleDefinition init(ScxAppInitContext context) {
        var path = context.config().get("template.root", ConfiguredPath.class).path();
        scxWeb=new ScxWeb();
        scxWeb.addReturnValueHandler(new TemplateReturnValueHandler(new TemplateEngine(path)));

        return ScxAppModuleDefinition.of()
            .componentSelector(c ->
                c.getAnnotation(Routes.class) != null
            ).startBefore(ScxAppHttpModule.class);
    }

    @Override
    public void start(ScxApp scxApp) {
        // todo 这里 需要 获取 di 容器中 所有类 然后 注入到 router 中
        ScxAppHttpModule httpModule = scxApp.getComponent(ScxAppHttpModule.class);
        Router router = httpModule.router();
        var l=new ArrayList<>();
        for (var stringComponentProviderEntry : scxApp.componentContainer().providers().entrySet()) {
            Class<?> aClass = stringComponentProviderEntry.getValue().componentType();
            if (aClass.getAnnotation(Routes.class)!=null){
                Object component = scxApp.getComponent(aClass);
                l.add(component);
            }
        }
        List<ScxWebRoute> routes = scxWeb.routes(l.toArray());
        for (ScxWebRoute route : routes) {
            router.route(route.priority(),route);
        }
    }

    public ScxWeb scxWeb() {
        return scxWeb;
    }

}
