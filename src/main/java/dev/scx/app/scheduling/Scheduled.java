package dev.scx.app.scheduling;

import java.lang.annotation.*;

/// 调度器注解
///
/// 目前仅支持 cron 形式.
///
/// @author scx567888
/// @version 0.0.1
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Scheduled.List.class)
public @interface Scheduled {

    String cron();

    /// {@link Scheduled} 的重复注解容器.
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {

        Scheduled[] value();

    }

}
