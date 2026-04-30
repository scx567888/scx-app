package dev.scx.app.x.static_server;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppModule;
import dev.scx.http.routing.Router;
import dev.scx.http.routing.x.static_files.StaticFilesHandler;

import java.lang.System.Logger;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;


/**
 * StaticServerModule
 *
 * @author scx567888
 * @version 0.0.1
 */
public class StaticServerModule extends ScxAppModule {

    private static final Logger logger = System.getLogger(StaticServerModule.class.getName());

    private static void registerStaticServerHandler(Router router, List<StaticServer> staticServers) {
        for (var staticServer : staticServers) {
            router.route(staticServer.location(), StaticFilesHandler.of(staticServer.root()));
        }
    }


    @Override
    public void start(ScxApp scx) {
        var staticServers = scx.scxConfig().get("static-servers", new ConvertStaticServerHandler(scx.scxEnvironment()));
        logger.log(DEBUG, "静态资源服务器 -->  {0}", staticServers.stream().map(StaticServer::location).collect(Collectors.joining(", ", "[", "]")));
        registerStaticServerHandler(scx.scxHttpRouter().router(), staticServers);
    }

    @Override
    public String name() {
        return "SCX_EXT-" + super.name();
    }

}
