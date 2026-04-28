package dev.scx.app;


import dev.scx.function.Function1Void;
import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.headers.HttpHeaderName;
import dev.scx.http.method.HttpMethod;
import dev.scx.http.routing.Route;
import dev.scx.http.routing.Router;
import dev.scx.http.routing.method_matcher.MethodMatcher;
import dev.scx.http.routing.path_matcher.PathMatcher;
import dev.scx.http.routing.route_table.PriorityRouteEntry;
import dev.scx.http.routing.x.cors.CorsHandler;
import dev.scx.http.routing.x.cors.allow_headers.AllowHeaders;
import dev.scx.http.routing.x.cors.allow_methods.AllowMethods;
import dev.scx.http.routing.x.cors.allow_origin.AllowOrigin;
import dev.scx.http.routing.x.cors.expose_headers.ExposeHeaders;
import dev.scx.web.ScxWebRoute;


import java.util.List;

import static dev.scx.http.headers.HttpHeaderName.*;
import static dev.scx.http.method.HttpMethod.*;

/// ScxHttp 路由 内部使用 Router 进行具体路由的处理
///
/// @author scx567888
/// @version 0.0.1
public final class ScxAppHttpRouter implements Function1Void<ScxHttpServerRequest, Throwable> {

    private static final HttpMethod[] DEFAULT_ALLOWED_METHODS = new HttpMethod[]{GET, POST, OPTIONS, DELETE, PATCH, PUT};
    private static final HttpHeaderName[] DEFAULT_ALLOWED_HEADERS = new HttpHeaderName[]{ACCEPT, CONTENT_TYPE};
    private static final HttpHeaderName[] DEFAULT_EXPOSED_HEADERS = new HttpHeaderName[]{CONTENT_DISPOSITION};

    //基本 handler
    private final CorsHandler corsHandler;
    //基本 handler 对应的 路由
    private final Route corsHandlerRoute;
    private final Router router;

    public ScxAppHttpRouter(ScxApp scx) {
        this.router=new Router();
        //设置基本的 handler
        this.corsHandler = initCorsHandler(scx.scxOptions().allowedOrigin());
        //注册路由
        this.corsHandlerRoute = Route.of(PathMatcher.any(), MethodMatcher.any(),corsHandler);
        router.route(-10000,this.corsHandlerRoute);
    }

    private static CorsHandler initCorsHandler(String allowedOriginPattern) {
        return CorsHandler.of()
                .allowOrigin(allowedOriginPattern.equals("*")?AllowOrigin.ofWildcard():AllowOrigin.of(allowedOriginPattern))
                .allowHeaders(AllowHeaders.of(DEFAULT_ALLOWED_HEADERS))
                .allowMethods(AllowMethods.of(DEFAULT_ALLOWED_METHODS))
                .exposeHeaders(ExposeHeaders.of(DEFAULT_EXPOSED_HEADERS))
                .allowCredentials(false);
    }

    public CorsHandler corsHandler() {
        return corsHandler;
    }

    public Route corsHandlerRoute() {
        return corsHandlerRoute;
    }

    public void add(List<ScxWebRoute> routes1) {
        for (ScxWebRoute scxWebRoute : routes1) {
            router.route(scxWebRoute.priority(),scxWebRoute);
        }
    }

    public List<PriorityRouteEntry> getRoutes() {
        return router.routeTable().entries();
    }

    @Override
    public void apply(ScxHttpServerRequest request) throws Throwable {
        router.apply(request);
    }

    public Router router() {
        return router;
    }
}
