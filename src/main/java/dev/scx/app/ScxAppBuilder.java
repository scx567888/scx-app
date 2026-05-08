package dev.scx.app;

import dev.scx.app.config.ArgsConfigSource;
import dev.scx.app.config.ScxConfig;
import dev.scx.app.config.ScxEnvironment;
import dev.scx.app.config.ScxConfigSource;

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


    /// 默认的核心包 APP KEY (密码) , 注意请不要在您自己的模块中使用此常量 , 非常不安全
    private static final String DEFAULT_APP_KEY = "SCX-123456";

    /// 默认配置文件 路径
    private static final String DEFAULT_SCX_CONFIG_PATH = "AppRoot:scx-config.json";

    /// 配置源
    private final List<ScxConfigSource> scxConfigSources = new ArrayList<>();

    /// 用来存储临时待添加的 外部参数
    private String[] args = new String[]{};

    /// 用来存储临时待添加的 mainClass
    private Class<?> mainClass = null;

    /// 用来存储临时待添加的 appKey
    private String appKey;


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
//        var defaultJsonFileConfigSource = JsonFileConfigSource.of(scxEnvironment.getPathByAppRoot(getScxConfigPath(args)));
        var defaultArgsConfigSource = ArgsConfigSource.of(args);
//        scxConfigSources.add(defaultJsonFileConfigSource);
        scxConfigSources.add(defaultArgsConfigSource);
        //创建 scx 实例
//        var scxConfig = new ScxConfig(scxConfigSources.toArray(ScxConfigSource[]::new));
        Path pathByAppRoot = scxEnvironment.getPathByAppRoot("AppRoot:scx-config.json");
        var scxConfig = ScxConfig.of(pathByAppRoot.toFile(),scxEnvironment);
        return new ScxApp(scxEnvironment, scxConfig, appModules.toArray(ScxAppModule[]::new));
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
