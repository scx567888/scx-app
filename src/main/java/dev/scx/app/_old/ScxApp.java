package dev.scx.app._old;

import dev.scx.app.ScxAppVersion;
import dev.scx.app._old.eventbus.EventBus;
import dev.scx.app._old.util.FileUtils;
import dev.scx.app._old.util.StopWatch;
import dev.scx.app._old.config.ScxConfig;
import dev.scx.app._old.config.ScxEnvironment;
import dev.scx.app._old.config.ScxFeatureConfig;
import dev.scx.ansi.Ansi;
import dev.scx.collection.ScxCollection;
import dev.scx.data.sql.schema_mapping.AnnotationConfigTable;
import dev.scx.di.ComponentContainer;
import dev.scx.http.ScxHttpServer;
import dev.scx.http.x.HttpServer;
import dev.scx.http.x.HttpServerOptions;
import dev.scx.http.x.error_handler.DefaultHttpServerErrorHandler;
import dev.scx.http.x.http1.headers.upgrade.Upgrade;
import dev.scx.sql.SQLClient;
import dev.scx.app._old.sql.TableSupport;
import dev.scx.tcp.tls.TLS;
import dev.scx.web.ScxWeb;
import dev.scx.web.ScxWebRoute;
import dev.scx.web.annotation.Routes;
import dev.scx.app.web.TemplateEngine;
import dev.scx.app.web.TemplateReturnValueHandler;
import dev.scx.websocket.x.WebSocketUpgradeRequestFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.BindException;
import java.net.Inet4Address;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import static dev.scx.app._old.ScxAppContext.GLOBAL_SCX;
import static dev.scx.app._old.ScxAppHelper.*;
import static dev.scx.app._old.enumeration.ScxAppFeature.*;
import static dev.scx.app._old.util.NetUtils.getLocalIPAddress;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/// 启动类
///
/// @author scx567888
/// @version 0.0.1
public final class ScxApp {

    static final Logger logger = System.getLogger(ScxApp.class.getName());

    /// 默认 http 请求 body 限制大小
    private static final long DEFAULT_BODY_LIMIT = FileUtils.displaySizeToLong("16384KB");
    private final ScxEnvironment scxEnvironment;

    private final String appKey;

    private final ScxFeatureConfig scxFeatureConfig;

    private final ScxConfig scxConfig;

    private final ScxAppModule[] scxModules;

    private final ScxAppOptions scxOptions;

    private final ComponentContainer beanFactory;

    private final ScxWeb scxWeb;

    private final Object defaultHttpServerOptions;

    private final EventBus eventBus;

    private SQLClient sqlClient = null;

    private ScxAppHttpRouter scxHttpRouter = null;

    private ScxHttpServer httpServer = null;

    ScxApp(ScxEnvironment scxEnvironment, String appKey, ScxFeatureConfig scxFeatureConfig, ScxConfig scxConfig, ScxAppModule[] scxModules, Object defaultHttpServerOptions) {
        //0, 赋值到全局
        ScxAppContext.scx(this);
        //1, 初始化基本参数
        this.scxEnvironment = scxEnvironment;
        this.appKey = appKey;
        this.scxFeatureConfig = scxFeatureConfig;
        this.scxConfig = scxConfig;
        this.scxModules = initScxModuleMetadataList(scxModules);
        this.scxOptions = new ScxAppOptions(this.scxConfig, this.scxEnvironment, this.appKey);
        this.defaultHttpServerOptions = defaultHttpServerOptions;
        //2, 初始化 ScxLog 日志框架
        initScxLoggerFactory(this.scxConfig, this.scxEnvironment);
        //3, 初始化 BeanFactory
        this.beanFactory = initBeanFactory(this.scxModules, this.scxFeatureConfig);
        //4, 初始化事件总线
        this.eventBus = new EventBus(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
        //4, 初始化 Web
        this.scxWeb = new ScxWeb();
        this.scxWeb.addReturnValueHandler(new TemplateReturnValueHandler(new TemplateEngine(scxOptions.templateRoot())));
    }

    public static ScxAppBuilder builder() {
        return new ScxAppBuilder();
    }

    /// 执行模块启动的生命周期
    private void startAllScxModules() {
        for (var m : scxModules) {
            if (this.scxFeatureConfig.get(SHOW_MODULE_LIFE_CYCLE_INFO)) {
                Ansi.ansi().brightWhite("[").brightGreen("Starting").brightWhite("] " + m.name()).println();
            }
            m.start(this);
            if (this.scxFeatureConfig.get(SHOW_MODULE_LIFE_CYCLE_INFO)) {
                Ansi.ansi().brightWhite("[").brightGreen("Start OK").brightWhite("] " + m.name()).println();
            }
        }
    }

    /// 执行模块结束的生命周期
    private void stopAllScxModules() {
        if (this.scxFeatureConfig.get(SHOW_MODULE_LIFE_CYCLE_INFO)) {
            for (var m : scxModules) {
                Ansi.ansi().brightWhite("[").brightRed("Stopping").brightWhite("] " + m.name()).println();
                m.stop(this);
                Ansi.ansi().brightWhite("[").brightRed("Stop  OK").brightWhite("] " + m.name()).println();
            }
        } else {
            for (var m : scxModules) {
                m.stop(this);
            }
        }
    }

    public ScxApp run() {
        return ScopedValue.where(GLOBAL_SCX, this).call(this::run0);
    }

    /// 运行项目
    private ScxApp run0() {
        //0, 启动 核心计时器
        StopWatch.start("ScxRun");
        //1, 根据配置打印一下 banner 或者配置文件信息之类
        if (this.scxFeatureConfig.get(SHOW_BANNER)) {
            ScxAppVersion.printBanner();
        }
        if (this.scxFeatureConfig.get(SHOW_OPTIONS_INFO)) {
            this.scxOptions.printInfo();
        }
        //2, 初始化路由器 (Http 和 WebSocket)
        this.scxHttpRouter = new ScxAppHttpRouter(this);
        //3, 注册 路由
        var classList = Arrays.stream(this.scxModules()).flatMap(c -> c.classList().stream()).toList();
        var httpRoutes = classList.stream().filter(c->c.getAnnotation(Routes.class)!=null).map(beanFactory::getComponent).toArray();

        List<ScxWebRoute> routes1 = this.scxWeb.routes(httpRoutes);
        scxHttpRouter.add(routes1);
        //4, 依次执行 模块的 start 生命周期 , 在这里我们可以操作 router 等对象 "手动注册新路由" 或其他任何操作
        this.startAllScxModules();
        //5, 打印基本信息
        if (this.scxFeatureConfig.get(SHOW_START_UP_INFO)) {
            var routes = this.scxHttpRouter.getRoutes();
            var entries = ScxCollection.countingBy(routes, c->c.route().requestMatcher());
//            var a = entries.get(RequestMatcher.any());
//            var b = entries.get(WebSocketTypeMatcher.NOT_WEB_SOCKET_HANDSHAKE);
//            var c = entries.get(WebSocketTypeMatcher.WEB_SOCKET_HANDSHAKE);
            Ansi.ansi()
                    .brightYellow("已加载 " + this.beanFactory.getComponentNames().length + " 个 Component !!!").ln()
                    .brightGreen("已加载 " + ((routes != null ? routes : 0)) + " 个 Http 路由 !!!").ln()
                    .brightBlue("已加载 " + (routes != null ? routes : 0) + " 个 WebSocket 路由 !!!").println();
        }
        //6, 初始化服务器
        this.httpServer = createServer();
        this.httpServer.onRequest(this.scxHttpRouter);
        //7, 添加程序停止时的钩子函数
        this.addShutdownHook();
        //8, 使用初始端口号 启动服务器
        this.startServer(this.scxOptions.port());
        //9, 此处刷新 scxBeanFactory 使其实例化所有符合条件的 Bean
        this.beanFactory.initializeComponents();
        //10, 启动调度器注解
        if (scxFeatureConfig.get(ENABLE_SCHEDULING_WITH_ANNOTATION)) {
            startAnnotationScheduled(this.beanFactory);
        }
        return this;
    }

    private ScxHttpServer createServer() {
        var httpServerOptions = (this.defaultHttpServerOptions != null ?
                new HttpServerOptions((HttpServerOptions) this.defaultHttpServerOptions) :
                new HttpServerOptions());

        httpServerOptions.maxPayloadSize(DEFAULT_BODY_LIMIT);

        if (this.scxOptions.isHttpsEnabled()) {
            var tls = TLS.of(this.scxOptions.sslPath(), this.scxOptions.sslPassword());
            httpServerOptions.tls(tls);
        }

        var hasWebSocketUpgradeHandler = httpServerOptions.http1ServerConnectionOptions().upgradeRequestFactories().get(Upgrade.WEB_SOCKET);
        //别忘了添加一个 websocket 处理器
        if (hasWebSocketUpgradeHandler==null) {
            httpServerOptions.http1ServerConnectionOptions().addUpgradeRequestFactory(new WebSocketUpgradeRequestFactory());
        }

        return new HttpServer(httpServerOptions).onError(new DefaultHttpServerErrorHandler(scxFeatureConfig.get(USE_DEVELOPMENT_ERROR_PAGE)));
    }

    /// 启动服务器
    ///
    /// @param port a int
    private void startServer(int port) {
        try {
            this.httpServer.start(port);
            var httpOrHttps = this.scxOptions.isHttpsEnabled() ? "https" : "http";
            var o = Ansi.ansi().green("服务器启动成功... 用时 " + StopWatch.stopToMillis("ScxRun") + " ms").ln();
            var p = this.httpServer.localAddress().getPort();
            o.green("> 本地: " + httpOrHttps + "://localhost:" + p + "/").ln();
            var normalIP = getLocalIPAddress(c -> c instanceof Inet4Address);
            for (var ip : normalIP) {
                o.green("> 网络: " + httpOrHttps + "://" + ip.getHostAddress() + ":" + p + "/").ln();
            }
            o.print();
        } catch (IOException e) {
            if (e instanceof BindException) {
                //获取新的端口号然后 重新启动服务器
                if (isUseNewPort(port)) {
                    startServer(0);
                }
            } else {
                e.printStackTrace();
            }
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.stopAllScxModules();
            Ansi.ansi().red("项目正在停止!!!").println();
        }));
    }

    /// 检查数据源是否可用
    ///
    /// @return b
    public boolean checkDataSource() {
        try (var conn = sqlClient().dataSource().getConnection()) {
            var dm = conn.getMetaData();
            logger.log(DEBUG, "数据源连接成功 : 类型 [{0}]  版本 [{1}]", dm.getDatabaseProductName(), dm.getDatabaseProductVersion());
            return true;
        } catch (Exception e) {
            dataSourceExceptionHandler(e);
            return false;
        }
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
                .filter(ScxAppHelper::isScxBaseModelClass)// 继承自 BaseModel
                .toList();
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

    @SuppressWarnings("unchecked")
    public <T extends ScxAppModule> T findScxModule(Class<T> clazz) {
        for (var m : this.scxModules) {
            if (m.getClass() == clazz) {
                return (T) m;
            }
        }
        return null;
    }

    public ScxAppModule[] scxModules() {
        return Arrays.copyOf(scxModules, scxModules.length);
    }

    public ScxEnvironment scxEnvironment() {
        return scxEnvironment;
    }

    public String appKey() {
        return appKey;
    }

    public ScxAppOptions scxOptions() {
        return scxOptions;
    }

    public ComponentContainer beanFactory() {
        return beanFactory;
    }

    public ScxAppHttpRouter scxHttpRouter() {
        return this.scxHttpRouter;
    }

    public ScxConfig scxConfig() {
        return scxConfig;
    }

    public ScxFeatureConfig scxFeatureConfig() {
        return scxFeatureConfig;
    }

    public DataSource dataSource() {
        return sqlClient().dataSource();
    }

    public SQLClient sqlClient() {
        if (sqlClient == null) {
            // 1, 初始化 sqlClient
            this.sqlClient = initSQLClient(this.scxOptions, this.scxFeatureConfig);
        }
        return sqlClient;
    }

    public ScxHttpServer httpServer() {
        return httpServer;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public ScxWeb scxWeb() {
        return scxWeb;
    }

    public <T> T getBean(Class<T> requiredType) {
        return beanFactory.getComponent(requiredType);
    }

}
