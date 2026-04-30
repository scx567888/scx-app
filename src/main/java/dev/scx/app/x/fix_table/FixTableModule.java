package dev.scx.app.x.fix_table;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppModule;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;

/**
 * FixTableModule
 *
 * @author scx567888
 * @version 0.0.1
 */
public class FixTableModule extends ScxAppModule {

    private static final System.Logger logger = System.getLogger(FixTableModule.class.getName());

    private static boolean confirmFixTable() {
        while (true) {
            var errMessage = """
                    *******************************************************
                    *                                                     *
                    *           Y 检测到需要修复数据表 , 是否修复 ?             *
                    *                                                     *
                    *         [Y]修复数据表  |  [N]忽略  |  [Q]退出           *
                    *                                                     *
                    *******************************************************
                    """;
            System.err.println(errMessage);
            var result = System.console().readLine().trim();
            if ("Y".equalsIgnoreCase(result)) {
                return true;
            } else if ("N".equalsIgnoreCase(result)) {
                return false;
            } else if ("Q".equalsIgnoreCase(result)) {
                System.exit(-1);
                return false;
            }
        }
    }

    @Override
    public void start(ScxApp scx) {
        if (!scx.checkDataSource()) {
            logger.log(ERROR, "数据源连接失败!!! 已跳过修复表!!!");
            return;
        }
        if (scx.checkNeedFixTable()) {
            if (confirmFixTable()) {
                scx.fixTable();
            } else {
                logger.log(DEBUG, "用户已取消修复表 !!!");
            }
        } else {
            logger.log(DEBUG, "没有表需要修复...");
        }
    }

    @Override
    public String name() {
        return "SCX_EXT-" + super.name();
    }

}
