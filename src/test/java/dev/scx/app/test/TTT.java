package dev.scx.app.test;

import dev.scx.app.*;
import dev.scx.app.component.ScxAppComponentModule;
import dev.scx.app.cors.ScxAppCorsModule;
import dev.scx.app.crud.CRUDModule;
import dev.scx.app.fix_table.FixTableModule;
import dev.scx.app.fss.FSSModule;
import dev.scx.app.http.ScxAppHttpModule;
import dev.scx.app.logging.ScxAppLoggingModule;
import dev.scx.app.redirect.RedirectModule;
import dev.scx.app.scheduling.ScxAppSchedulingModule;
import dev.scx.app.sql.ScxAppSQLModule;
import dev.scx.app.static_server.ScxAppStaticServerModule;
import dev.scx.app.web.ScxAppWebModule;

import java.io.IOException;
import java.util.List;

public class TTT implements ScxAppModule {
    @Override
    public ScxAppModuleDefinition init(ScxAppInitContext context) {
        List<Class<?>> classListByScxModule = null;
        try {
            classListByScxModule = ScxAppHelper.findClassListByScxModule(this.getClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ScxAppModuleDefinition.of()
            .candidate(classListByScxModule.toArray(Class[]::new));
    }

    static void main(String[] args) {
        ScxApp.builder()
            .setMainClass(TTT.class)
            .module(new ScxAppLoggingModule())
            .module(new ScxAppComponentModule())
            .module(new ScxAppHttpModule())
            .module(new ScxAppCorsModule())
            .module(new ScxAppWebModule())
            .module(new ScxAppStaticServerModule())
            .module(new ScxAppSQLModule())
            .module(new ScxAppSchedulingModule())
            // 以下是 偏业务的模块
            .module(new FixTableModule())
            .module(new FSSModule())
            .module(new RedirectModule())
            .module(new CRUDModule())
            .module(new TTT())
            .build()
            .run();
    }

}
