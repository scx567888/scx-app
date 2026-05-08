package dev.scx.app.static_server;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppInitContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;
import dev.scx.app.http.ScxAppHttpModule;
import dev.scx.http.routing.Router;
import dev.scx.http.routing.x.static_files.StaticFilesHandler;
import dev.scx.web.annotation.Routes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;

public class ScxAppStaticServerModule implements ScxAppModule {


    private static final System.Logger logger = System.getLogger(ScxAppStaticServerModule.class.getName());

    private static void registerStaticServerHandler(Router router, StaticServer[] staticServers) {
        for (var staticServer : staticServers) {
            router.route(staticServer.location(), StaticFilesHandler.of(staticServer.root().path()));
        }
    }

    @Override
    public ScxAppModuleDefinition init(ScxAppInitContext context) {
        return ScxAppModuleDefinition.of()
            .componentSelector(c ->
                c.getAnnotation(Routes.class) != null
            );
    }

    @Override
    public void start(ScxApp scx) {
        var staticServers = scx.scxConfig().get("static-servers", StaticServer[].class,new StaticServer[0]);
        logger.log(DEBUG, "静态资源服务器 -->  {0}", Arrays.stream(staticServers).map(StaticServer::location).collect(Collectors.joining(", ", "[", "]")));
        registerStaticServerHandler(scx.getComponent(ScxAppHttpModule.class).router(), staticServers);
    }

}
