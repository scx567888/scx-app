package dev.scx.app;

import dev.scx.app.cors.ScxAppCorsModule;
import dev.scx.app.http.ScxAppHttpModule;
import dev.scx.app.static_server.ScxAppStaticServerModule;
import dev.scx.app.web.ScxAppWebModule;

public class TTT {

    static void main(String[] args) {
        ScxApp.builder()
            .module(new ScxAppHttpModule())
            .module(new ScxAppCorsModule())
            .module(new ScxAppWebModule())
            .module(new ScxAppStaticServerModule())
            .build()
            .run();
    }

}
