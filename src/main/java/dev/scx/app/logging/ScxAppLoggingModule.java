package dev.scx.app.logging;

import dev.scx.app.ScxAppInitContext;
import dev.scx.app.ScxAppModule;
import dev.scx.app.ScxAppModuleDefinition;
import dev.scx.app.ScxAppHelper;
import dev.scx.app.config.ConfiguredPath;
import dev.scx.app.util.ObjectUtils;
import dev.scx.app.config.ScxConfig;
import dev.scx.app.config.ScxEnvironment;
import dev.scx.logging.ScxLoggerConfig;
import dev.scx.logging.ScxLogging;
import dev.scx.logging.recorder.ConsoleRecorder;
import dev.scx.logging.recorder.FileRecorder;
import dev.scx.reflect.TypeReference;
import dev.scx.serialize.ScxSerialize;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.System.Logger.Level.*;
import static java.lang.System.Logger.Level.ALL;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.util.Objects.requireNonNull;

public final class ScxAppLoggingModule implements ScxAppModule {

    @Override
    public ScxAppModuleDefinition init(ScxAppInitContext context) {
        initScxLoggerFactory0(context.config(),context.environment());
        return ScxAppModuleDefinition.of();
    }

    private static void initScxLoggerFactory0(ScxConfig scxConfig, ScxEnvironment scxEnvironment) {
        Path pathByAppRoot = scxEnvironment.getPathByAppRoot("AppRoot:logs");
        //先初始化好 DefaultScxLoggerInfo
        var defaultLevel = toLevel(scxConfig.get("scx.logging.default.level", String.class));
        var defaultType = toType(scxConfig.get("scx.logging.default.type", String.class));
        var defaultStoredDirectory = scxConfig.get("scx.logging.default.stored-directory", ConfiguredPath.class);
        var defaultStackTrace = scxConfig.get("scx.logging.default.stack-trace", boolean.class,false);

        //设置默认的 config 这里我们先清除所有的 Recorders
        var defaultConfig = ScxLogging.rootConfig().clearRecorders();
        defaultConfig.setLevel(defaultLevel);
        if (defaultType == LoggingType.CONSOLE || defaultType == LoggingType.BOTH) {
            defaultConfig.addRecorder(new ConsoleRecorder());
        }
        if (defaultType == LoggingType.FILE || defaultType == LoggingType.BOTH) {
            defaultConfig.addRecorder(new FileRecorder(defaultStoredDirectory.path()));
        }
        defaultConfig.setStackTrace(defaultStackTrace);

        //以下日志若有缺少的 storedDirectory 则全部以 defaultStoredDirectory 为准
        var loggers = scxConfig.get("scx.logging.loggers", new TypeReference<List<Map<String, String>>>() {
        });
        if (loggers != null) {
            for (var logger : loggers) {
                var name = logger.get("name");
                if (ObjectUtils.notBlank(name)) {
                    var level = toLevel(logger.get("level"));
                    var type = toType(logger.get("type"));
                    var storedDirectory = ObjectUtils.notBlank(logger.get("stored-directory")) ? scxEnvironment.getPathByAppRoot(logger.get("stored-directory")) : null;
                    var stackTrace = ScxSerialize.convertObject(logger.get("stack-trace"), Boolean.class);
                    var config = new ScxLoggerConfig();
                    config.setLevel(level);
                    if (type == LoggingType.CONSOLE || type == LoggingType.BOTH) {
                        config.addRecorder(new ConsoleRecorder());
                    }
                    if (type == LoggingType.FILE || type == LoggingType.BOTH) {
                        //文件路径的缺省值使用 默认的
                        config.addRecorder(new FileRecorder(storedDirectory != null ? storedDirectory : defaultStoredDirectory.path()));
                    }
                    config.setStackTrace(stackTrace);
                    ScxLogging.setConfig(name, config);
                }
            }
        }
    }

    private static System.Logger.Level toLevel(String levelName) {
        Objects.requireNonNull(levelName, "levelName 不能为空 !!!");
        var s = levelName.trim().toUpperCase();
        return switch (s) {
            case "OFF", "O" -> OFF;
            case "ERROR", "E" -> ERROR;
            case "WARN", "WARNING", "W" -> WARNING;
            case "INFO", "I" -> INFO;
            case "DEBUG", "D" -> DEBUG;
            case "TRACE", "T" -> TRACE;
            case "ALL", "A" -> ALL;
            default -> null;
        };
    }

    private static LoggingType toType(String loggingTypeName) {
        requireNonNull(loggingTypeName, "loggingTypeName 不能为空 !!!");
        var s = loggingTypeName.trim().toUpperCase();
        return switch (s) {
            case "CONSOLE", "C" -> LoggingType.CONSOLE;
            case "FILE", "F" -> LoggingType.FILE;
            case "BOTH", "B" -> LoggingType.BOTH;
            default -> null;
        };
    }

    private enum LoggingType {

        /// 打印到控制台
        CONSOLE,

        /// 写入到文件
        FILE,

        /// 既打印到控制台也同时写入到文件
        BOTH
    }

}
