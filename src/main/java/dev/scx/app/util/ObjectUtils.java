package dev.scx.app.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static dev.scx.array.ScxArray.toWrapper;

/// 处理对象的简易工具类
///
/// @author scx567888
/// @version 0.0.1
public final class ObjectUtils {

    /// 将嵌套的 map 扁平化
    ///
    /// @param sourceMap 源 map
    /// @param parentKey a [java.lang.String] object.
    /// @return 扁平化后的 map
    private static Map<String, Object> flatMap0(Map<?, ?> sourceMap, String parentKey) {
        var result = new LinkedHashMap<String, Object>();
        var prefix = ObjectUtils.isBlank(parentKey) ? "" : parentKey + ".";
        sourceMap.forEach((key, value) -> {
            var newKey = prefix + key;
            if (value instanceof Map<?, ?> m) {
                result.putAll(flatMap0(m, newKey));
            } else {
                result.put(newKey, value);
            }
        });
        return result;
    }

    /// 将嵌套的 map 扁平化
    ///
    /// @param sourceMap 源 map
    /// @return 扁平化后的 map
    public static Map<String, Object> flatMap(Map<?, ?> sourceMap) {
        return flatMap0(sourceMap, null);
    }

    /// null -> true
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof Optional<?> optional) {
            return optional.isEmpty();
        } else if (obj instanceof CharSequence charSequence) {
            return charSequence.isEmpty();
        } else if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        } else if (obj instanceof Collection<?> collection) {
            return collection.isEmpty();
        } else if (obj instanceof Map<?, ?> map) {
            return map.isEmpty();
        } else {
            return false;
        }
    }

    /// 校验字符串是否不为 null 且不全为空白 (空格 " ")
    ///
    /// @param str 待检查的字符串
    /// @return a boolean.
    public static boolean notBlank(String str) {
        return !isBlank(str);
    }

    /// 校验字符串是否为 null 或全为空白 (空格 " ")
    ///
    /// @param str a [java.lang.Object] object.
    /// @return a boolean.
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /// 校验字符串是否不为 null 并且不为空字符串 ("")
    ///
    /// @param str s
    /// @return s
    public static boolean notEmpty(String str) {
        return !isEmpty(str);
    }

    /// 校验字符串是否为 null 或为空字符串 ("")
    ///
    /// @param str s
    /// @return s
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }


    /// 联接字符串 但是不连接空
    public static String concat(String... strings) {
        var sb = new StringBuilder();
        for (var string : strings) {
            if (string != null) {
                sb.append(string);
            }
        }
        return sb.toString();
    }



    public static String removeQuotes(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        int start = 0;
        int end = str.length();

        if (str.charAt(0) == '\"') {
            start = start + 1;
        }

        if (str.charAt(end - 1) == '\"') {
            end = end - 1;
        }

        return str.substring(start, end);
    }

    /// null -> true
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }



}
