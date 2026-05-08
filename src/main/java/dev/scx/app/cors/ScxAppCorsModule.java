package dev.scx.app.cors;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppInitContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;
import dev.scx.app.http.ScxAppHttpModule;
import dev.scx.http.headers.HttpHeaderName;
import dev.scx.http.method.HttpMethod;
import dev.scx.http.routing.Route;
import dev.scx.http.routing.Router;
import dev.scx.http.routing.method_matcher.MethodMatcher;
import dev.scx.http.routing.path_matcher.PathMatcher;
import dev.scx.http.routing.x.cors.CorsHandler;
import dev.scx.http.routing.x.cors.allow_headers.AllowHeaders;
import dev.scx.http.routing.x.cors.allow_methods.AllowMethods;
import dev.scx.http.routing.x.cors.allow_origin.AllowOrigin;
import dev.scx.http.routing.x.cors.expose_headers.ExposeHeaders;

import static dev.scx.http.headers.HttpHeaderName.*;
import static dev.scx.http.method.HttpMethod.*;
import static dev.scx.http.method.HttpMethod.DELETE;
import static dev.scx.http.method.HttpMethod.PATCH;
import static dev.scx.http.method.HttpMethod.PUT;

public class ScxAppCorsModule implements ScxAppModule {

    private static final HttpMethod[] DEFAULT_ALLOWED_METHODS = new HttpMethod[]{GET, POST, OPTIONS, DELETE, PATCH, PUT};
    private static final HttpHeaderName[] DEFAULT_ALLOWED_HEADERS = new HttpHeaderName[]{ACCEPT, CONTENT_TYPE};
    private static final HttpHeaderName[] DEFAULT_EXPOSED_HEADERS = new HttpHeaderName[]{CONTENT_DISPOSITION};


    //基本 handler
    private  CorsHandler corsHandler;
    //基本 handler 对应的 路由
    private  Route corsHandlerRoute;

    @Override
    public ScxAppModuleDefinition init(ScxAppInitContext context) {
        return ScxAppModuleDefinition.of().startBefore(ScxAppHttpModule.class);
    }

    @Override
    public void start(ScxApp scxApp) {
        ScxAppHttpModule httpModule = scxApp.getComponent(ScxAppHttpModule.class);
        Router router = httpModule.router();

        var allowedOrigin=scxApp.scxConfig().get("allowedOrigin", String.class);
        //设置基本的 handler
        this.corsHandler = initCorsHandler(allowedOrigin);
        //注册路由
        this.corsHandlerRoute = Route.of(PathMatcher.any(), MethodMatcher.any(),corsHandler);
        router.route(-10000,this.corsHandlerRoute);

    }

    private static CorsHandler initCorsHandler(String allowedOriginPattern) {
        if (allowedOriginPattern==null){
            allowedOriginPattern="*";
        }
        return CorsHandler.of()
            .allowOrigin(allowedOriginPattern.equals("*")? AllowOrigin.ofWildcard():AllowOrigin.of(allowedOriginPattern))
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

}
