package dev.scx.app;

import dev.scx.app._old.config.ScxFeatureConfig;
import dev.scx.app._old.config.source.ArgsConfigSource;
import dev.scx.app._old.config.source.MapConfigSource;
import dev.scx.app.config.ScxConfig;
import dev.scx.app.config.ScxEnvironment;
import dev.scx.config.ScxConfigSource;

import java.nio.file.Path;
import java.util.*;

public class ScxAppBuilder {

    private List<ScxAppModule> appModules;

    public ScxAppBuilder() {
        this.appModules = new ArrayList<>();
    }

    public ScxAppBuilder module(ScxAppModule... appModules) {
        Collections.addAll(this.appModules, appModules);
        return this;
    }







    /// 默认配置键值对, 以便在没有配置文件的时候可以使项目正确启动
    private static final Map<String, Object> DEFAULT_CONFIG_MAP = initDefaultConfigMap();

    /// 默认的核心包 APP KEY (密码) , 注意请不要在您自己的模块中使用此常量 , 非常不安全
    private static final String DEFAULT_APP_KEY = "SCX-123456";

    /// 默认配置文件 路径
    private static final String DEFAULT_SCX_CONFIG_PATH = "AppRoot:scx-config.json";

    /// 用来存储临时待添加的 scxModules
    private final List<ScxAppModule> scxModules = new ArrayList<>();

    /// 用来存储临时待添加的 scxFeatureConfig
    private final ScxFeatureConfig scxFeatureConfig = new ScxFeatureConfig();

    /// 配置源
    private final List<ScxConfigSource> scxConfigSources = new ArrayList<>();

    /// 用来存储临时待添加的 外部参数
    private String[] args = new String[]{};

    /// 用来存储临时待添加的 mainClass
    private Class<?> mainClass = null;

    /// 用来存储临时待添加的 appKey
    private String appKey;

    /// a
    ///
    /// @return a
    private static Map<String, Object> initDefaultConfigMap() {
        var tempMap = new LinkedHashMap<String, Object>();
        tempMap.put("scx.port", 8080);
        tempMap.put("scx.tombstone", false);
        tempMap.put("scx.allowed-origin", "*");
        tempMap.put("scx.template.root", "AppRoot:/c/");
        tempMap.put("scx.static-servers", new Object[0]);
        tempMap.put("scx.https.enabled", false);
        tempMap.put("scx.https.ssl-path", "");
        tempMap.put("scx.https.ssl-password", "");
        tempMap.put("scx.data-source.host", "127.0.0.1");
        tempMap.put("scx.data-source.port", 3306);
        tempMap.put("scx.data-source.database", "");
        tempMap.put("scx.data-source.username", "");
        tempMap.put("scx.data-source.password", "");
        tempMap.put("scx.data-source.parameters", new HashSet<>());
        tempMap.put("scx.logging.default.level", "ERROR");
        tempMap.put("scx.logging.default.type", "CONSOLE");
        tempMap.put("scx.logging.default.stored-directory", "AppRoot:logs");
        tempMap.put("scx.logging.default.stack-trace", false);
        return tempMap;
    }

    private static Class<?> checkMainClass(Class<?> mainClass) {
        //1,检测 mainClass 是否正确
        if (mainClass == null) {
            throw new IllegalArgumentException("MainClass must not be empty !!! ");
        }
        return mainClass;
    }

    private static String getScxConfigPath(String[] args) {
//        var scxConfig = ScxConfig(ArgsConfigSource.of(args));
        var scxConfig = ScxConfig.of(null,null);
        var scxConfigPath = scxConfig.get("scx.config.path", String.class);
        return scxConfigPath != null ? scxConfigPath : DEFAULT_SCX_CONFIG_PATH;
    }

    /// a
    public ScxApp run() {
        ScxApp build = this.build();
        build.run();
        return build;
    }

    /// 构建
    ///
    /// @return a
    public ScxApp build() {
        //检查 mainClass
        checkMainClass(mainClass);
        //处理数据源
        var scxEnvironment = new ScxEnvironment(mainClass);
        //配置源 注意顺序 以保证可以逐个覆盖
        var defaultMapConfigSource = MapConfigSource.of(DEFAULT_CONFIG_MAP);
//        var defaultJsonFileConfigSource = JsonFileConfigSource.of(scxEnvironment.getPathByAppRoot(getScxConfigPath(args)));
        var defaultArgsConfigSource = ArgsConfigSource.of(args);
        scxConfigSources.add(defaultMapConfigSource);
//        scxConfigSources.add(defaultJsonFileConfigSource);
        scxConfigSources.add(defaultArgsConfigSource);
        //创建 scx 实例
//        var scxConfig = new ScxConfig(scxConfigSources.toArray(ScxConfigSource[]::new));
        Path pathByAppRoot = scxEnvironment.getPathByAppRoot("AppRoot:scx-config.json");
        var scxConfig = ScxConfig.of(pathByAppRoot.toFile(),scxEnvironment);
        return new ScxApp(scxEnvironment, scxConfig, scxModules.toArray(ScxAppModule[]::new));
    }

    /// 添加多个模块
    ///
    /// @param mainClass a
    /// @return a
    public ScxAppBuilder setMainClass(Class<?> mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    /// 添加多个模块
    ///
    /// @param appKey a
    /// @return a
    public ScxAppBuilder setAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    /// 添加 外部参数
    ///
    /// @param args a
    /// @return a
    public ScxAppBuilder setArgs(String... args) {
        this.args = args;
        return this;
    }

}
