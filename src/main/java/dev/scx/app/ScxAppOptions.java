package dev.scx.app;

import dev.scx.app.config.ScxConfig;
import dev.scx.app.config.ScxEnvironment;
import dev.scx.app.config.handler.AppRootHandler;
import dev.scx.app.config.handler.DecryptValueHandler;
import dev.scx.ansi.Ansi;
import dev.scx.app.util.NetUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static dev.scx.app.util.NetUtils.getLocalIPAddress;

/// ScxOptions
///
/// @author scx567888
/// @version 0.0.1
public final class ScxAppOptions {

    /// 端口号
    private final int port;

    /// 允许的 域
    private final String allowedOrigin;

    /// 数据源地址
    private final String dataSourceUrl;

    /// 数据源 用户名
    private final String dataSourceUsername;

    /// 数据源密码 真实值(解密后)
    private final String dataSourcePassword;

    /// 其他连接参数
    private final String[] dataSourceParameters;

    /// 是否开启 https
    private final boolean httpsEnabled;

    /// ssl 证书路径 字符串值
    private final Path sslPath;

    /// ssl 证书密码 (解密后)
    private final String sslPassword;

    /// 模板 根目录 字符串值
    private final Path templateRoot;

    public ScxAppOptions(ScxConfig scxConfig, ScxEnvironment scxEnvironment, String appKey) {
        port = scxConfig.getOrDefault("scx.port", 8080);
        allowedOrigin = scxConfig.getOrDefault("scx.allowed-origin", "*");
        templateRoot = scxConfig.get("scx.template.root", AppRootHandler.of(scxEnvironment, "AppRoot:/c/"));
        httpsEnabled = scxConfig.getOrDefault("scx.https.enabled", false);
        sslPath = scxConfig.get("scx.https.ssl-path", AppRootHandler.of(scxEnvironment));
        sslPassword = scxConfig.get("scx.https.ssl-password", DecryptValueHandler.of(appKey));
        dataSourceUrl = scxConfig.getOrDefault("scx.data-source.url", "");
        dataSourceUsername = scxConfig.get("scx.data-source.username", String.class);
        dataSourcePassword = scxConfig.get("scx.data-source.password", DecryptValueHandler.of(appKey));
        dataSourceParameters = scxConfig.getOrDefault("scx.data-source.parameters", new String[]{});
    }

    public String dataSourceUrl() {
        return dataSourceUrl;
    }

    public String[] dataSourceParameters() {
        return dataSourceParameters;
    }

    public String dataSourceUsername() {
        return dataSourceUsername;
    }

    public String dataSourcePassword() {
        return dataSourcePassword;
    }

    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    public int port() {
        return port;
    }

    public Path sslPath() {
        return sslPath;
    }

    public String sslPassword() {
        return sslPassword;
    }

    public Path templateRoot() {
        return templateRoot;
    }

    public String allowedOrigin() {
        return allowedOrigin;
    }

    public void printInfo() {
        String ip= "[]";
        try {
            ip = Arrays.stream(NetUtils.getLocalIPAddress(c -> c instanceof Inet4Address)).map(InetAddress::getHostAddress).collect(Collectors.joining(", ", "[", "]"));
        } catch (SocketException _) {

        }
        Ansi.ansi()
                .green("Y 服务器 IP 地址                       \t -->\t " +ip ).ln()
                .green("Y 端口号                               \t -->\t " + port).ln()
                .green("Y 允许的请求源                         \t -->\t " + allowedOrigin).ln()
                .green("Y 模板根目录                           \t -->\t " + templateRoot.toString()).ln()
                .green("Y 是否开启 https                       \t -->\t " + (httpsEnabled ? "是" : "否")).ln()
                .green("Y 证书路径                            \t -->\t " + (sslPath != null ? sslPath.toString() : "")).ln()
                .green("Y 证书密码                            \t -->\t *****").ln()
                .green("Y 数据源 URL                           \t -->\t " + dataSourceUrl).ln()
                .green("Y 数据源 用户名                        \t -->\t " + dataSourceUsername).ln()
                .green("Y 数据源 连接密码                      \t -->\t *****").ln()
                .green("Y 数据源 连接参数                      \t -->\t " + Arrays.toString(dataSourceParameters)).println();
    }

}
