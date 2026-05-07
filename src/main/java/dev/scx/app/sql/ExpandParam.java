package dev.scx.app.sql;

import java.util.Collection;
import java.util.List;

/// 可展开的 SQL 参数类型, 用于处理带有列表形式的命名参数.
///
/// 通常用于 SQL 查询中, 如 IN 子句 例如: `SELECT * FROM table WHERE field IN (:values)``
/// 该类允许用户将多个值传递给 SQL 查询, 而不是仅仅传递一个单一值.
///
/// 注意: 此参数类型只能用于带有命名参数的 SQL 查询, 且仅适用于 NamedSQL.
///
/// 示例:
///
/// ```java
/// var inList = new ExpandParam(1, 2, 3, 4);
/// ```
///
/// 对应 SQL 查询: `SELECT * FROM table WHERE field IN (:inList)`
///
/// ## 注意 !!!
/// 空 values 可能会生成非法 SQL 或无效 SQL.
///
/// @author scx567888
/// @version 0.0.1
/// @see NamedSQL
public record ExpandParam(Collection<?> values) {

    public static ExpandParam of(Object... values) {
        return new ExpandParam(List.of(values));
    }

    public static ExpandParam of(Collection<?> values) {
        return new ExpandParam(values);
    }

}
