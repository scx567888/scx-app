package dev.scx.app.scheduling;

import dev.scx.app.ScxAppModule;
import dev.scx.di.ComponentContainer;
import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.ScxReflect;
import dev.scx.scheduling.ScxScheduling;

import java.util.Arrays;
import java.util.stream.Stream;

import static dev.scx.reflect.AccessModifier.PUBLIC;
import static java.lang.System.Logger.Level.ERROR;

public class ScxAppSchedulingModule implements ScxAppModule {

    public static void startAnnotationScheduled(ComponentContainer beanFactory) {
        var beanDefinitionNames = beanFactory.getComponentNames();
        for (var beanDefinitionName : beanDefinitionNames) {
            var bean = beanFactory.getComponent(beanDefinitionName);
            // todo 这里强转可能有问题
            var classInfo = (ClassInfo) ScxReflect.typeOf(bean.getClass());
            for (var method : classInfo.methods()) {
                if (method.accessModifier() != PUBLIC) {
                    continue;
                }
                var scheduledList = Arrays.stream(method.annotations()).flatMap(c -> switch (c) {
                    case Scheduled s -> Stream.of(s);
                    case Scheduled.List f -> Stream.of(f.value());
                    default -> Stream.of();
                }).toList();
                for (Scheduled scheduled : scheduledList) {
                    if (method.parameters().length != 0) {
                        ScxApp.logger.log(ERROR,
                            "被 Scheduled 注解标识的方法不可以有参数 Class [{0}] , Method [{1}]",
                            classInfo.name(),
                            method.name()
                        );
                        break;
                    }
                    if (method.isStatic()) {
                        ScxScheduling.cron()
                            .cronExpression(scheduled.cron())
                            .start(c -> {
                                try {
                                    method.invoke(null);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    } else {
                        ScxScheduling.cron()
                            .cronExpression(scheduled.cron())
                            .start(c -> {
                                try {
                                    method.invoke(bean);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    }
                }

            }
        }
    }

}
