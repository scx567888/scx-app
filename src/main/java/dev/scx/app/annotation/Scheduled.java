package dev.scx.app.annotation;

import java.lang.annotation.*;

/// 调度器注解
///
/// @author scx567888
/// @version 0.0.1
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ScheduledList.class)
public @interface Scheduled {

    String cron();

}
