package dev.scx.app._old.x.test;


import dev.scx.app._old.ScxApp;
import dev.scx.app._old.ScxAppModule;
import dev.scx.app._old.enumeration.ScxAppFeature;
import dev.scx.app._old.x.crud.CRUDModule;
import dev.scx.app._old.x.fix_table.FixTableModule;
import dev.scx.app._old.x.fss.FSSModule;
import dev.scx.app._old.x.redirect.RedirectModule;
import dev.scx.app._old.x.static_server.StaticServerModule;

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
