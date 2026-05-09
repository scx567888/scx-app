package dev.scx.app._util.zip;

import dev.scx.app._util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/// URIUtils
///
/// @author scx567888
/// @version 0.0.1
public final class URIUtils {

    /// 切割正则表达式
    private static final Pattern PATH_SEPARATOR = Pattern.compile("[/\\\\]+");

    /// 拼接多个 uri 并进行一些简单的清理  例 : 处理前 ["a/b/", "/c"] 处理后 "a/b/c"
    ///
    /// @param uris 需要清理的 uri 集合
    /// @return 拼接后的结果
    public static String join(String... uris) {
        return normalize(String.join("/", Arrays.stream(uris).filter(ObjectUtils::notBlank).toArray(String[]::new)));
    }

    public static String join(Collection<String> uris) {
        return normalize(String.join("/", uris.stream().filter(ObjectUtils::notBlank).toArray(String[]::new)));
    }

    /// 移除两端的 "/" 或 "\"
    /// 注意不要和 {@link java.lang.String#trim()}  混淆 此方法不处理空格 只处理斜杠
    ///
    /// @param uri a
    /// @return a
    public static String trimSlash(String uri) {
        var value = uri.toCharArray();
        int length = value.length;
        int st = findSlashStart(value);
        int len = findSlashEnd(value);
        return st > 0 || len < length ? new String(Arrays.copyOfRange(value, st, len)) : uri;
    }

    public static String trimSlashStart(String uri) {
        var value = uri.toCharArray();
        int st = findSlashStart(value);
        return st > 0 ? new String(Arrays.copyOfRange(value, st, value.length)) : uri;
    }

    public static String trimSlashEnd(String uri) {
        var value = uri.toCharArray();
        int len = findSlashEnd(value);
        return len < value.length ? new String(Arrays.copyOfRange(value, 0, len)) : uri;
    }

    private static int findSlashStart(char[] value) {
        int length = value.length;
        int st = 0;
        while (st < length && (value[st] == '/' || value[st] == '\\')) {
            st = st + 1;
        }
        return st;
    }

    private static int findSlashEnd(char[] value) {
        int len = value.length;
        int st = 0;
        while (st < len && (value[len - 1] == '/' || value[len - 1] == '\\')) {
            len = len - 1;
        }
        return len;
    }

    public static String addSlashStart(String uri) {
        return "/" + trimSlashStart(uri);
    }

    public static String addSlashEnd(String uri) {
        return trimSlashEnd(uri) + "/";
    }

    /// 将 \ 分割的全部转换为 / 并清除多余的 /
    ///
    /// @param uri a
    /// @return a
    public static String normalize(String uri) {
        var chars = uri.toCharArray();
        var index = 0;
        var isSeparator = false;
        for (var c : chars) {
            if (c == '/' || c == '\\') {
                if (!isSeparator) {
                    chars[index] = '/';
                    index = index + 1;
                }
                isSeparator = true;
            } else {
                chars[index] = c;
                index = index + 1;
                isSeparator = false;
            }
        }
        return new String(chars, 0, index);
    }

    public static String[] split(String uri) {
        return PATH_SEPARATOR.split(uri, -1);
    }

}
