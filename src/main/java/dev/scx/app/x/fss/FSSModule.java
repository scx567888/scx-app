package dev.scx.app.x.fss;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppModule;

import java.lang.System.Logger;

/**
 * 提供基本的文件上传及下载 (展示)的功能
 *
 * @author scx567888
 * @version 0.0.1
 */
public class FSSModule extends ScxAppModule {

    private static final Logger logger = System.getLogger(FSSModule.class.getName());

    public FSSModule() {

    }

    @Override
    public void start(ScxApp scx) {
        FSSConfig.initConfig(scx);
    }

    @Override
    public String name() {
        return "SCX_EXT-" + super.name();
    }

}
