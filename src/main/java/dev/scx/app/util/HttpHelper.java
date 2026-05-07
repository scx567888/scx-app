package dev.scx.app.util;

import static dev.scx.app.util.ObjectUtils.notBlank;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

/// HttpHelper
///
/// @author scx567888
/// @version 0.0.1
public class HttpHelper {

    /// 获取访问者IP
    ///
    /// 先从Header中获取X-Real-IP, 如果不存在再从X-Forwarded-For获得第一个IP(用,分割),
    /// 如果还不存在则调用 HttpServerRequest.remoteAddress()
    ///
    /// @param request a
    /// @return IP
    public static String getRequestIP(dev.scx.http.ScxHttpServerRequest request) {

        var xRealIPStr = request.getHeader("X-Real-IP");
        if (notBlank(xRealIPStr) && !"unknown".equalsIgnoreCase(xRealIPStr)) {
            return xRealIPStr;
        }

        var xForwardedForStr = request.getHeader("X-Forwarded-For");
        if (notBlank(xForwardedForStr) && !"unknown".equalsIgnoreCase(xForwardedForStr)) {
            var s = xForwardedForStr.split(",");
            if (s.length > 0) {
                return s[0];
            }
        }

        var remoteAddressStr = request.remotePeer().address().toString();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddressStr)) {
            return "127.0.0.1";
        } else {
            return remoteAddressStr;
        }

    }

    /// todo 这是一个 hack
    /// URLEncoder.encode 针对 ' ' (空格) 会编码为 '+' , 而这里我们需要的是编码为 %20
    ///
    /// @param downloadName a [java.lang.String] object
    /// @return c
    /// @see <a href="https://www.rfc-editor.org/rfc/rfc6266.html">https://www.rfc-editor.org/rfc/rfc6266.html</a>
    public static String getDownloadContentDisposition(String downloadName) {
        return "attachment; filename*=utf-8''" + encode(downloadName, UTF_8).replace("+", "%20");
    }


}
