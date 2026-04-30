package dev.scx.app.x.crud.serialization;

import dev.scx.data.query.*;
import dev.scx.format.NodeToFormatException;
import dev.scx.object.ObjectToNodeException;
import dev.scx.data.query.Query;
import dev.scx.serialize.ScxSerialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// QuerySerializer
///
/// @author scx567888
/// @version 0.0.1
public class QuerySerializer {

    public static String serializeQueryToJson(Query query) throws SerializationException {
        var v = serializeQuery(query);
        try {
            return ScxSerialize.toJson(v);
        } catch (ObjectToNodeException|NodeToFormatException e) {
            throw new SerializationException(e);
        }
    }

    public static Map<String, Object> serializeQuery(Query query) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "Query");
        m.put("where", serializeWhere(query.getWhere()));
        m.put("orderBys", serializeOrderBys(query.getOrderBys()));
        m.put("offset", query.getOffset());
        m.put("limit", query.getLimit());
        return m;
    }

    public static Map<String, Object> serializeWhere(Where obj) {
        return switch (obj) {
            case WhereClause w -> serializeWhereClause(w);
            case And a -> serializeAnd(a);
            case Or o -> serializeOr(o);
            case Not n -> serializeNot(n);
            case Condition conditionBody -> serializeCondition(conditionBody);
            case null -> null;
            default -> throw new IllegalArgumentException("Unknown Where type: " + obj);
        };
    }

    private static Map<String, Object> serializeWhereClause(WhereClause w) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "WhereClause");
        m.put("expression", w.expression());
        m.put("params", w.params());
        return m;
    }

    private static Map<String, Object> serializeAnd(And a) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "And");
        m.put("clauses", serializeWhereAll(a.clauses()));
        return m;
    }

    private static Map<String, Object> serializeOr(Or o) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "Or");
        m.put("clauses", serializeWhereAll(o.clauses()));
        return m;
    }

    private static Map<String, Object> serializeNot(Not n) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "Not");
        m.put("clause", serializeWhere(n.clause()));
        return m;
    }

    private static Map<String, Object> serializeCondition(Condition w) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "Condition");
        m.put("selector", w.selector());
        m.put("conditionType", w.conditionType());
        m.put("value1", w.value1());
        m.put("value2", w.value2());
        m.put("useExpression", w.useExpression());
        m.put("useExpressionValue", w.useExpressionValue());
        m.put("skipIfInfo", serializeSkipIfInfo(w.skipIfInfo()));
        return m;
    }

    private static List<Object> serializeWhereAll(Where[] objs) {
        var s = new ArrayList<>();
        for (var obj : objs) {
            s.add(serializeWhere(obj));
        }
        return s;
    }

    public static List<Object> serializeOrderBys(OrderBy[] objs) {
        var s = new ArrayList<>();
        for (var obj : objs) {
            s.add(serializeOrderBy(obj));
        }
        return s;
    }

    private static Map<String, Object> serializeOrderBy(OrderBy orderByBody) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "OrderBy");
        m.put("selector", orderByBody.selector());
        m.put("orderByType", orderByBody.orderByType());
        m.put("useExpression", orderByBody.useExpression());
        return m;
    }

    private static Map<String, Object> serializeSkipIfInfo(SkipIfInfo info) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "SkipIfInfo");
        m.put("skipIfNull", info.skipIfNull());
        m.put("skipIfEmptyList", info.skipIfEmptyList());
        m.put("skipIfEmptyString", info.skipIfEmptyString());
        m.put("skipIfBlankString", info.skipIfBlankString());
        return m;
    }

}
