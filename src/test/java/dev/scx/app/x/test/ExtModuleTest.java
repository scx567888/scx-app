package dev.scx.app.x.test;


import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppModule;
import dev.scx.app.enumeration.ScxAppFeature;
import dev.scx.app.x.crud.CRUDModule;
import dev.scx.app.x.fix_table.FixTableModule;
import dev.scx.app.x.fss.FSSModule;
import dev.scx.app.x.redirect.RedirectModule;
import dev.scx.app.x.static_server.StaticServerModule;

public class ExtModuleTest extends ScxAppModule {

    public static void main(String[] args) {
        test1();
    }

//    @Test
    public static void test1() {
        ScxApp.builder()
                .setMainClass(ExtModuleTest.class)
                .addModule(
                        new ExtModuleTest(),
                        new CRUDModule(),
                        new FixTableModule(),
                        new FSSModule(),
                        new StaticServerModule(),
                        new RedirectModule()
                )
                .configure(ScxAppFeature.USE_DEVELOPMENT_ERROR_PAGE, true)
                .configure(ScxAppFeature.USE_SPY, true)
                .setArgs("--scx.config.path=AppRoot:scx-config (2).json")
                .run();
    }

    @Override
    public void start(ScxApp scx) {
        scx.fixTable();
    }

}
