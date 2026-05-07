package dev.scx.app.sql;

import dev.scx.sql.BatchSQL;
import dev.scx.sql.DefaultBatchSQL;
import dev.scx.sql.DefaultSQL;
import dev.scx.sql.SQL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/// NamedSQLCompiler
///
/// @author scx567888
/// @version 0.0.1
final class NamedSQLCompiler {

    /// 具名参数匹配的正则表达式
    private static final Pattern NAMED_SQL_PATTERN = Pattern.compile(":([\\w.-]+)");

    /// 创建重复的 ? (带分隔符)
    public static String placeholders(int count) {
        if (count == 0) {
            return "";
        }
        var element = "?, ";
        var result = element.repeat(count);
        return result.substring(0, result.length() - ", ".length());
    }

    public static SQL compile(String namedSQL, Map<String, Object> params) {
        // 匹配所有命名参数
        var matcher = NAMED_SQL_PATTERN.matcher(namedSQL);
        var tempSQL = new StringBuilder();
        var tempParams = new ArrayList<>();
        while (matcher.find()) {
            // 这里 需要根据 参数类型来做特殊处理
            var g = matcher.group(1);
            var value = params.get(g);
            // 如果是 特殊参数 我们特殊处理 进行展开
            if (value instanceof ExpandParam(Collection<?> values)) {
                tempParams.addAll(values);
                matcher.appendReplacement(tempSQL, placeholders(values.size()));
            } else {// 其余则使用普通 ?
                tempParams.add(value);
                matcher.appendReplacement(tempSQL, "?");
            }
        }
        matcher.appendTail(tempSQL);

        return new DefaultSQL(tempSQL.toString(), tempParams.toArray());
    }

    public static BatchSQL compileBatch(String namedSQL, List<Map<String, Object>> batchParams) {
        // 匹配所有命名参数
        var matcher = NAMED_SQL_PATTERN.matcher(namedSQL);
        var tempSQL = new StringBuilder();
        var tempNameIndex = new ArrayList<String>();
        while (matcher.find()) {
            // 批量时 我们不支持特殊参数 所以统一使用 ? 来表示
            var g = matcher.group(1);
            tempNameIndex.add(g);
            matcher.appendReplacement(tempSQL, "?");
        }
        matcher.appendTail(tempSQL);

        // 这里将 map 转换为数组
        var nameIndex = tempNameIndex.toArray(String[]::new);
        var tempBatchParams = new ArrayList<Object[]>();
        for (var p : batchParams) {
            var o = new Object[nameIndex.length];
            for (int i = 0; i < nameIndex.length; i = i + 1) {
                o[i] = p.get(nameIndex[i]);
            }
            tempBatchParams.add(o);
        }

        return new DefaultBatchSQL(tempSQL.toString(), tempBatchParams);
    }

}
