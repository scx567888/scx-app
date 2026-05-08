package dev.scx.app.fix_table;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppModule;
import dev.scx.app.base.BaseModel;
import dev.scx.app.sql.TableSupport;
import dev.scx.app.util.ClassUtils;
import dev.scx.data.sql.annotation.Table;
import dev.scx.data.sql.schema_mapping.AnnotationConfigTable;
import dev.scx.sql.SQLClient;

import java.util.Arrays;
import java.util.List;

import static java.lang.System.Logger.Level.*;

/**
 * FixTableModule
 *
 * @author scx567888
 * @version 0.0.1
 */
public class FixTableModule implements ScxAppModule {

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
        SQLClient sqlClient = scx.getComponent(SQLClient.class);
        if (!checkDataSource(sqlClient)) {
            logger.log(ERROR, "数据源连接失败!!! 已跳过修复表!!!");
            return;
        }

        if (checkNeedFixTable(sqlClient)) {
            if (confirmFixTable()) {
                fixTable(sqlClient);
            } else {
                logger.log(DEBUG, "用户已取消修复表 !!!");
            }
        } else {
            logger.log(DEBUG, "没有表需要修复...");
        }
    }

    /// 检查是否有任何 (BaseModel) 类需要修复表
    ///
    /// @return 是否有
    public boolean checkNeedFixTable(SQLClient sqlClient) {
        logger.log(DEBUG, "检查数据表结构中...");
        for (var v : getAllScxBaseModelClassList()) {
            //根据 class 获取 tableInfo
            var tableInfo = new AnnotationConfigTable<>(v);
            try {
                //有任何需要修复的直接 返回 true
                if (TableSupport.checkNeedFixTable(tableInfo, sqlClient)) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void fixTable(SQLClient sqlClient) {
        logger.log(DEBUG, "修复数据表结构中...");
        //修复成功的表
        var fixSuccess = 0;
        //修复失败的表
        var fixFail = 0;
        //不需要修复的表
        var noNeedToFix = 0;
        for (var v : getAllScxBaseModelClassList()) {
            //根据 class 获取 tableInfo
            var tableInfo = new AnnotationConfigTable<>(v);
            try {
                if (TableSupport.checkNeedFixTable(tableInfo, sqlClient)) {
                    TableSupport.fixTable(tableInfo, sqlClient);
                    fixSuccess = fixSuccess + 1;
                } else {
                    noNeedToFix = noNeedToFix + 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
                fixFail = fixFail + 1;
            }
        }

        if (fixSuccess != 0) {
            logger.log(DEBUG, "修复成功 {0} 张表...", fixSuccess);
        }
        if (fixFail != 0) {
            logger.log(WARNING, "修复失败 {0} 张表...", fixFail);
        }
        if (fixSuccess + fixFail == 0) {
            logger.log(DEBUG, "没有表需要修复...");
        }

    }



    /// 获取所有 class
    ///
    /// @return s
    private List<Class<?>> getAllScxBaseModelClassList() {
//        return Arrays.stream(scxModules)
//            .flatMap(c -> c.classList().stream())
//            .filter(FixTableModule::isScxBaseModelClass)// 继承自 BaseModel
//            .toList();
        // todo
        return List.of();
    }

    /// 初始化 ScxModelClassList
    ///
    /// @param c a
    /// @return a
    public static boolean isScxBaseModelClass(Class<?> c) {
        return c.isAnnotationPresent(Table.class) ;
    }

    /// 检查数据源是否可用
    ///
    /// @return b
    public boolean checkDataSource(SQLClient sqlClient) {
        try (var conn = sqlClient.dataSource().getConnection()) {
            var dm = conn.getMetaData();
            logger.log(DEBUG, "数据源连接成功 : 类型 [{0}]  版本 [{1}]", dm.getDatabaseProductName(), dm.getDatabaseProductVersion());
            return true;
        } catch (Exception e) {
            dataSourceExceptionHandler(e);
            return false;
        }
    }

    /// 数据源连接异常
    ///
    /// @param e a [java.lang.Exception] object.
    static void dataSourceExceptionHandler(Exception e) {
        while (true) {
            var errMessage = """
                **************************************************************
                *                                                            *
                *           X 数据源连接失败 !!! 是否忽略错误并继续运行 ?            *
                *                                                            *
                *        [Y] 忽略错误并继续运行    |     [N] 退出程序              *
                *                                                            *
                **************************************************************
                """;
            System.err.println(errMessage);
            var result = System.console().readLine().trim();
            if ("Y".equalsIgnoreCase(result)) {
                var ignoreMessage = """
                    *******************************************
                    *                                         *
                    *       N 数据源链接错误,用户已忽略 !!!         *
                    *                                         *
                    *******************************************
                    """;
                System.err.println(ignoreMessage);
                break;
            } else if ("N".equalsIgnoreCase(result)) {
                e.printStackTrace();
                System.exit(-1);
                break;
            }
        }
    }

}
