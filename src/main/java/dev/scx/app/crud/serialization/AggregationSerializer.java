package dev.scx.app.crud.serialization;

import dev.scx.data.aggregation.*;
import dev.scx.format.NodeToFormatException;
import dev.scx.object.ObjectToNodeException;
import dev.scx.serialize.ScxSerialize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AggregationSerializer {

    public static String serializeAggregationToJson(Aggregation aggregation) throws SerializationException {
        var v = serializeAggregation(aggregation);
        try {
            return ScxSerialize.toJson(v);
        } catch (ObjectToNodeException| NodeToFormatException e) {
            throw new SerializationException(e);
        }
    }

    public static Map<String, Object> serializeAggregation(Aggregation aggregation) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "Aggregation");
        m.put("groupBys", serializeGroupBys(aggregation.getGroupBys()));
        m.put("aggs", serializeAggs(aggregation.getAggs()));
        return m;
    }

    private static Map<String, Object> serializeFieldGroupBy(FieldGroupBy groupBy) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "FieldGroupBy");
        m.put("fieldName", groupBy.fieldName());
        return m;
    }

    private static Map<String, Object> serializeExpressionGroupBy(ExpressionGroupBy groupBy) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "ExpressionGroupBy");
        m.put("alias", groupBy.alias());
        m.put("expression", groupBy.expression());
        return m;
    }

    private static Map<String, Object> serializeAgg(Agg g) {
        var m = new LinkedHashMap<String, Object>();
        m.put("@type", "Agg");
        m.put("alias", g.alias());
        m.put("expression", g.expression());
        return m;
    }

    private static List<Map<String, Object>> serializeGroupBys(GroupBy[] groupBys) {
        var m = new ArrayList<Map<String, Object>>();
        for (var groupBy : groupBys) {
            m.add(serializeGroupBy(groupBy));
        }
        return m;
    }

    private static Map<String, Object> serializeGroupBy(GroupBy g) {
        return switch (g) {
            case FieldGroupBy fieldGroupBy -> serializeFieldGroupBy(fieldGroupBy);
            case ExpressionGroupBy fieldGroupBy -> serializeExpressionGroupBy(fieldGroupBy);
        };
    }

    private static List<Map<String, Object>> serializeAggs(Agg[] aggs) {
        var m = new ArrayList<Map<String, Object>>();
        for (var agg : aggs) {
            m.add(serializeAgg(agg));
        }
        return m;
    }

}
