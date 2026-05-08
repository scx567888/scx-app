package dev.scx.app.fss;


import dev.scx.app.ScxApp;
import dev.scx.app._old.config.handler.AppRootHandler;

import java.lang.System.Logger;
import java.nio.file.Path;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * 核心模块配置文件
 *
 * @author scx567888
 * @version 0.0.1
 */
public class FSSConfig {

    private static final Logger logger = System.getLogger(FSSConfig.class.getName());

    private static Path uploadFilePath;

    static void initConfig(ScxApp scx) {
        AppRootHandler appRootHandler = AppRootHandler.of(scx.scxEnvironment(), "AppRoot:/FSS_FILES/");
        uploadFilePath = scx.scxConfig().get("fss.physical-file-path", Path.class);
        logger.log(DEBUG, "FSS 物理文件存储位置  -->  {0}", uploadFilePath);
    }

    public static Path uploadFilePath() {
        return uploadFilePath;
    }

}
