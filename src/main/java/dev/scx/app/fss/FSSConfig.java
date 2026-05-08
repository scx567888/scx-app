package dev.scx.app.fss;


import dev.scx.app.ScxApp;
import dev.scx.app.config.ConfiguredPath;

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
        var appRootHandler = scx.scxEnvironment().getPathByAppRoot("AppRoot:/FSS_FILES/");
        var p = scx.scxConfig().get("fss.physical-file-path", ConfiguredPath.class);
        if (p==null){
            uploadFilePath=appRootHandler;
        }else{
            uploadFilePath=p.path();
        }
        logger.log(DEBUG, "FSS 物理文件存储位置  -->  {0}", uploadFilePath);
    }

    public static Path uploadFilePath() {
        return uploadFilePath;
    }

}
