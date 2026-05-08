package dev.scx.app;

import dev.scx.data.exception.DataAccessException;
import dev.scx.di.ComponentContainer;
import dev.scx.function.Function0;
import dev.scx.function.Function0Void;
import dev.scx.function.Function1;
import dev.scx.function.Function1Void;
import dev.scx.http.ScxHttpServer;
import dev.scx.sql.SQLClient;
import dev.scx.sql.SQLTransaction;
import dev.scx.transaction.exception.TransactionException;
import dev.scx.web.ScxWeb;

import javax.sql.DataSource;
import java.nio.file.Path;

/// 用来存储 整个项目的上下文
///
/// @author scx567888
/// @version 0.0.1
public final class ScxAppContext {

    /// 全局唯一的 SCX APP
    /// 为了保证方法使用的简易 我们建议使用静态的方法
    /// 但是其本质上是调用 GLOBAL_UNIQUE_SCX_APP 方法中的实例对象
    static ScxApp GLOBAL_SCX ;

    /// 获取全局的 Scx
    ///
    /// @return scx
    public static ScxApp scx() {
        if (GLOBAL_SCX!=null) {
            return GLOBAL_SCX;
        } else {
            throw new RuntimeException("全局 Scx 未初始化 !!! 请先使用 Scx.builder() 创建 Scx 实例 , 全局 Scx 会自动设置 !!!");
        }
    }




    public static SQLClient sqlClient() {
        return scx().getComponent(SQLClient.class);
    }

    public static <X extends Throwable> void autoTransaction(Function0Void<X> handler) throws X, DataAccessException {
        sqlClient().autoTransaction(handler);
    }

    public static <R, X extends Throwable> R autoTransaction(Function0<R, X> handler) throws X, DataAccessException {
        return sqlClient().autoTransaction(handler);
    }

    public static  <R, X extends Throwable> R withTransaction(Function1<SQLTransaction, R, X> handler) throws TransactionException, X {
        return sqlClient().withTransaction(handler);
    }

    public static <X extends Throwable> void withTransaction(Function1Void<SQLTransaction, X> handler) throws TransactionException, X {
        sqlClient().withTransaction(handler);
    }

    public static <T> T getBean(Class<T> requiredType) {
        return scx().getComponent(requiredType);
    }

    public static SQLClient jdbcContext() {
        return scx().getComponent(SQLClient.class);
    }

    public static Path getTempPath(String file) {
        return scx().scxEnvironment().getTempPath(file);
    }
}
