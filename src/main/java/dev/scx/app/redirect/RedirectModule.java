package dev.scx.app.redirect;

import dev.scx.ansi.Ansi;
import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppModule;
import dev.scx.http.uri.ScxURI;
import dev.scx.http.x.HttpServer;

import java.lang.System.Logger;

import static dev.scx.http.headers.HttpHeaderName.LOCATION;
import static dev.scx.http.status_code.HttpStatusCode.TEMPORARY_REDIRECT;

/**
 * 监听 80 端口并将所有 http 请求重定向 到 https
 *
 * @author scx567888
 * @version 0.0.1
 */
public class RedirectModule implements ScxAppModule {

    private static final Logger logger = System.getLogger(RedirectModule.class.getName());

    private final int port;

    public RedirectModule() {
        this(80);
    }

    public RedirectModule(int port) {
        this.port = port;
    }

    /**
     * 也可以直接以工具类的形式调用
     *
     * @param port a int
     */
    public static void startRedirect(int port) {
        var httpServer = new HttpServer();
        httpServer.onRequest(request -> {
            var oldURI = request.uri();
            var newURI = ScxURI.of(oldURI).scheme("https");
            request.response()
                .setHeader(LOCATION, newURI.encode(true))
                .statusCode(TEMPORARY_REDIRECT)
                .send();
        });

        try {
            httpServer.start(port);
            Ansi.ansi().brightMagenta("转发服务器启动成功 http -> https, 端口号 : " + httpServer.localAddress().getPort() + " !!!").println();
        } catch (Exception e) {
            logger.log(Logger.Level.ERROR, "转发服务器启动失败 !!! ", e);
        }
    }

    @Override
    public void start(ScxApp scx) {
        //只有当开启 https 的时候才进行转发
        if (scx.scxOptions().isHttpsEnabled()) {
            startRedirect(this.port);
        }
    }

}
