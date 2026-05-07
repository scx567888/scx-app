package dev.scx.app.fix_table;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppModule;
import dev.scx.app.base.BaseModel;
import dev.scx.app.sql.TableSupport;
import dev.scx.app.util.ClassUtils;
import dev.scx.data.sql.annotation.Table;
import dev.scx.data.sql.schema_mapping.AnnotationConfigTable;

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
        if (!scx.checkDataSource()) {
            logger.log(ERROR, "数据源连接失败!!! 已跳过修复表!!!");
            return;
        }
        if (checkNeedFixTable()) {
            if (confirmFixTable()) {
                fixTable();
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
    public boolean checkNeedFixTable() {
        logger.log(DEBUG, "检查数据表结构中...");
        for (var v : getAllScxBaseModelClassList()) {
            //根据 class 获取 tableInfo
            var tableInfo = new AnnotationConfigTable<>(v);
            try {
                //有任何需要修复的直接 返回 true
                if (TableSupport.checkNeedFixTable(tableInfo, sqlClient())) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void fixTable() {
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
                if (TableSupport.checkNeedFixTable(tableInfo, sqlClient())) {
                    TableSupport.fixTable(tableInfo, sqlClient());
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
        return Arrays.stream(scxModules)
            .flatMap(c -> c.classList().stream())
            .filter(FixTableModule::isScxBaseModelClass)// 继承自 BaseModel
            .toList();
    }

    /// 初始化 ScxModelClassList
    ///
    /// @param c a
    /// @return a
    public static boolean isScxBaseModelClass(Class<?> c) {
        return c.isAnnotationPresent(Table.class) &&  // 拥有注解
            ClassUtils.isInstantiableClass(c) &&  // 是一个可以不需要其他参数直接生成实例化的对象
            BaseModel.class.isAssignableFrom(c);
    }

}
