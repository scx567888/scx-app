package dev.scx.app;

import dev.scx.app.component.ScxAppComponentModule;
import dev.scx.app.cors.ScxAppCorsModule;
import dev.scx.app.fix_table.FixTableModule;
import dev.scx.app.fss.FSSModule;
import dev.scx.app.http.ScxAppHttpModule;
import dev.scx.app.logging.ScxAppLoggingModule;
import dev.scx.app.scheduling.ScxAppSchedulingModule;
import dev.scx.app.sql.ScxAppSQLModule;
import dev.scx.app.static_server.ScxAppStaticServerModule;
import dev.scx.app.web.ScxAppWebModule;

public class TTT {

    static void main(String[] args) {
        ScxApp.builder()
            .module(new ScxAppLoggingModule())
            .module(new ScxAppComponentModule())
            .module(new ScxAppHttpModule())
            .module(new ScxAppCorsModule())
            .module(new ScxAppWebModule())
            .module(new ScxAppStaticServerModule())
            .module(new ScxAppSQLModule())
            .module(new ScxAppSchedulingModule())
            .module(new FixTableModule())
            .module(new FSSModule())
            .build()
            .run();
    }

}
