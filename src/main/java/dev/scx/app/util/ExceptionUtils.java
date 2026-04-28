package dev.scx.app.util;

import dev.scx.function.Function0;
import dev.scx.function.Function0Void;

import java.io.PrintWriter;
import java.io.StringWriter;

/// ExceptionUtils
///
/// @author scx567888
/// @version 0.0.1
public final class ExceptionUtils {

    /// 忽略异常 发生异常时返回 null
    public static <T> T ignore(Function0<T, ?> handler) {
        try {
            return handler.apply();
        } catch (Throwable throwable) {
            return null;
        }
    }

    /// 忽略异常 发生异常时返回默认值
    public static <T> T ignore(Function0<T, ?> handler, T defaultVal) {
        try {
            return handler.apply();
        } catch (Throwable throwable) {
            return defaultVal;
        }
    }

    /// 忽略异常
    public static void ignore(Function0Void<?> handler) {
        try {
            handler.apply();
        } catch (Throwable ignored) {

        }
    }

    /// 检测执行的操作是否有异常 (有异常时不打印信息)
    public static boolean noException(Function0Void<?> scxRunnable) {
        try {
            scxRunnable.apply();
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    /// 获取 jdk 内部默认实现的堆栈跟踪字符串
    public static String getStackTraceString(Throwable throwable) {
        var stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.getBuffer().toString();
    }

}
