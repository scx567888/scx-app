package dev.scx.app.util.os;


import static dev.scx.app.util.os.OSType.*;

/// OSHelper 用来获取操作系统信息
///
/// @author scx567888
/// @version 0.0.1
public final class OSHelper {

    private static final OSInfo osInfo = initOSInfo();

    private static OSInfo initOSInfo() {
        var osName = System.getProperty("os.name");
        var osVersion = System.getProperty("os.version");
        return new OSInfo(getOSType(osName), osVersion);
    }

    private static OSType getOSType(String osName) {
        if (osName.startsWith("Linux")) {
            if ("dalvik".equalsIgnoreCase(System.getProperty("java.vm.name"))) {
                return ANDROID;
            } else {
                return LINUX;
            }
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            return MAC;
        } else if (osName.startsWith("Windows")) {
            return WINDOWS;
        } else {
            return UNKNOWN;
        }
    }

    public static OSInfo getOSInfo() {
        return osInfo;
    }

    public static boolean isMac() {
        return osInfo.type() == MAC;
    }

    public static boolean isAndroid() {
        return osInfo.type() == ANDROID;
    }

    public static boolean isLinux() {
        return osInfo.type() == LINUX;
    }

    public static boolean isWindows() {
        return osInfo.type() == WINDOWS;
    }

}
