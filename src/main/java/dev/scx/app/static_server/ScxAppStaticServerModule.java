package dev.scx.app.static_server;

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

public class ScxAppStaticServerModule implements ScxAppModule {


    private static final System.Logger logger = System.getLogger(ScxAppStaticServerModule.class.getName());

    private static void registerStaticServerHandler(Router router, List<StaticServer> staticServers) {
        for (var staticServer : staticServers) {
            router.route(staticServer.location(), StaticFilesHandler.of(staticServer.root()));
        }
    }

    @Override
    public ScxAppModuleDefinition define(ScxAppDefineContext context) {
        return new ScxAppModuleDefinition()
            .componentSelector(c ->
                c.getAnnotation(Routes.class) != null
            );
    }

    @Override
    public void init(ScxApp scx) {
        var staticServers = scx.scxConfig().get("static-servers", new ConvertStaticServerHandler(scx.scxEnvironment()));
        logger.log(DEBUG, "静态资源服务器 -->  {0}", staticServers.stream().map(StaticServer::location).collect(Collectors.joining(", ", "[", "]")));
        registerStaticServerHandler(scx.scxHttpRouter().router(), staticServers);
    }

}
