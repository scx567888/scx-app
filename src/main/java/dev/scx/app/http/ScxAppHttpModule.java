package dev.scx.app.http;

import dev.scx.ansi.Ansi;
import dev.scx.app.ScxAppDefineContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;
import dev.scx.app._old.ScxApp;
import dev.scx.app._old.util.StopWatch;
import dev.scx.http.ScxHttpServer;
import dev.scx.http.routing.Router;
import dev.scx.http.x.HttpServer;
import dev.scx.http.x.HttpServerOptions;
import dev.scx.http.x.error_handler.DefaultHttpServerErrorHandler;
import dev.scx.http.x.http1.headers.upgrade.Upgrade;
import dev.scx.tcp.tls.TLS;
import dev.scx.web.ScxWeb;
import dev.scx.web.annotation.Routes;
import dev.scx.websocket.x.WebSocketUpgradeRequestFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.Inet4Address;
import java.nio.file.Path;

import static dev.scx.app._old.enumeration.ScxAppFeature.USE_DEVELOPMENT_ERROR_PAGE;
import static dev.scx.app._old.util.NetUtils.getLocalIPAddress;

public class ScxAppHttpModule implements ScxAppModule {

    private static final long DEFAULT_BODY_LIMIT = 1024 * 1024 * 16;

    private final HttpServerOptions defaultHttpServerOptions;
    private ScxHttpServer httpServer;
    private Router router;

    public ScxAppHttpModule() {
        this.defaultHttpServerOptions = null;
    }

    public ScxAppHttpModule(HttpServerOptions defaultHttpServerOptions) {
        this.defaultHttpServerOptions = defaultHttpServerOptions;
    }

    @Override
    public void init(ScxApp scxApp) {
        var httpServerOptions = this.defaultHttpServerOptions != null ?
            new HttpServerOptions(this.defaultHttpServerOptions) :
            new HttpServerOptions();

        httpServerOptions.maxPayloadSize(DEFAULT_BODY_LIMIT);

        var httpsEnabled = scxApp.scxConfig().get("xxxxx", boolean.class);
        var sslPath = scxApp.scxConfig().get("xxxxx", Path.class);
        var sslPassword = scxApp.scxConfig().get("xxxxx", String.class);

        if (httpsEnabled) {
            var tls = TLS.of(sslPath, sslPassword);
            httpServerOptions.tls(tls);
        }

        var hasWebSocketUpgradeHandler = httpServerOptions
            .http1ServerConnectionOptions()
            .upgradeRequestFactories()
            .get(Upgrade.WEB_SOCKET);

        //别忘了添加一个 websocket 处理器
        if (hasWebSocketUpgradeHandler == null) {
            httpServerOptions.http1ServerConnectionOptions().addUpgradeRequestFactory(new WebSocketUpgradeRequestFactory());
        }

        this.router=Router.of();

        httpServer = new HttpServer(httpServerOptions)
            .onRequest(router)
            .onError(new DefaultHttpServerErrorHandler(scxApp.scxFeatureConfig().get(USE_DEVELOPMENT_ERROR_PAGE)));
    }

    @Override
    public void start(ScxApp scxApp) {
        this.startServer(scxApp.scxConfig().get("port",int.class),scxApp);
    }

    private void startServer(int port, ScxApp scxApp) {
        try {
            this.httpServer.start(port);
            var httpsEnabled = scxApp.scxConfig().get("xxxxx", boolean.class);
            var httpOrHttps = httpsEnabled ? "https" : "http";
            var o = Ansi.ansi().green("服务器启动成功... 用时 " + StopWatch.stopToMillis("ScxRun") + " ms").ln();
            var p = this.httpServer.localAddress().getPort();
            o.green("> 本地: " + httpOrHttps + "://localhost:" + p + "/").ln();
            var normalIP = getLocalIPAddress(c -> c instanceof Inet4Address);
            for (var ip : normalIP) {
                o.green("> 网络: " + httpOrHttps + "://" + ip.getHostAddress() + ":" + p + "/").ln();
            }
            o.print();
        } catch (IOException e) {
            if (e instanceof BindException) {
                //获取新的端口号然后 重新启动服务器
                if (isUseNewPort(port)) {
                    startServer(0,scxApp);
                }
            } else {
                e.printStackTrace();
            }
        }
    }


    /// 获取新的可用的端口号 (使用弹窗让用户进行选择)
    ///
    /// @param port a
    /// @return a
    static boolean isUseNewPort(int port) {
        while (true) {
            var errMessage = """
                *******************************************************
                *                                                     *
                *         端口号 [ %s ] 已被占用, 是否采用新端口号 ?       *
                *                                                     *
                *                [Y]es    |    [N]o                   *
                *                                                     *
                *******************************************************
                """;
            System.err.printf((errMessage) + System.lineSeparator(), port);
            var result = System.console().readLine().trim();
            if ("Y".equalsIgnoreCase(result)) {
                return true;
            } else if ("N".equalsIgnoreCase(result)) {
                var ignoreMessage = """
                    *******************************************
                    *                                         *
                    *     N 端口号被占用!!! 服务器启动失败 !!!      *
                    *                                         *
                    *******************************************
                    """;
                System.err.println(ignoreMessage);
                System.exit(-1);
                return false;
            }
        }
    }

    public ScxHttpServer httpServer() {
        return httpServer;
    }

    public Router router() {
        return router;
    }

}
