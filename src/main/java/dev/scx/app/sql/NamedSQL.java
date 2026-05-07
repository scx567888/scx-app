package dev.scx.app.sql;

import dev.scx.sql.BatchSQL;
import dev.scx.sql.SQL;

import java.util.List;
import java.util.Map;

/// 代表带有具名参数的 SQL 查询. 通过此类, 用户可以方便地构造 SQL 查询, 使用命名参数代替传统的 `?` 占位符.
///
/// 具名参数通过 `:` 后跟参数名 (如 `:paramName`) 表示. 在执行 SQL 查询时, 具名参数会被其实际值替换,
/// 提供了更具可读性的 SQL 语句.
///
/// 特别地, `NamedSQL` 类支持两种使用方式:
/// 1. **单个 SQL 查询**: 构造一个带有具名参数的 SQL 查询, 支持替换具名参数为相应的值.
/// 2. **批量查询**: 在批量操作 (如批量插入、更新) 时, 支持将不同的参数集合对应到 SQL 查询中的具名参数.
///
///
/// **特殊情况: 处理 ExpandParam**
/// 当 SQL 查询中包含类型为 `ExpandParam` 的具名参数时 (例如 `IN` 子句),
/// 该类会自动展开这个参数, 生成多个 `?` 占位符并将其值作为多个参数传入查询中.
///
///
/// 示例:
///
/// ```java
/// var params = Map.of(
///   "inList", ExpandParam.of(1, 2, 3)
/// );
/// var namedSQL = NamedSQL.namedSQL("SELECT * FROM table WHERE field IN (:inList)", params);
/// ```
///
/// 对应 SQL 查询: `SELECT * FROM table WHERE field IN (?, ?, ?)`
///
/// ## 注意 !!!
/// 仅做轻量命名参数替换，不解析完整 SQL 语法。
/// 因此以下场景可能误判或不受支持：
/// - 字符串字面量中的冒号
/// - 注释中的冒号
/// - PostgreSQL `::` cast
/// - 其他包含冒号但并非具名参数的位置
///
/// @author scx567888
/// @version 0.0.1
/// @see ExpandParam
public final class NamedSQL {

    public static SQL namedSQL(String namedSQL, Map<String, Object> params) {
        return NamedSQLCompiler.compile(namedSQL, params);
    }

    public static BatchSQL namedBatchSQL(String namedSQL, List<Map<String, Object>> batchParams) {
        return NamedSQLCompiler.compileBatch(namedSQL, batchParams);
    }

}
