package dev.scx.app.crud;

import dev.scx.app.crud.serialization.DeserializationException;
import dev.scx.data.field_policy.FieldPolicy;
import dev.scx.data.query.Query;
import dev.scx.http.exception.BadRequestException;
import dev.scx.node.Node;

import static dev.scx.app.crud.serialization.FieldPolicyDeserializer.deserializeFieldPolicy;
import static dev.scx.app.crud.serialization.QueryDeserializer.deserializeQuery;
import static dev.scx.data.field_policy.FieldPolicyBuilder.includeAll;
import static dev.scx.data.query.QueryBuilder.query;
import static dev.scx.node.NullNode.NULL;


/**
 * CRUDListParam
 *
 * @author scx567888
 * @version 0.0.1
 */
public class CRUDListParam {

    public Node query;

    public Node fieldPolicy;

    /**
     * 拓展参数
     */
    public Node extParams;

    public Query getQuery() {
        if (query == null || query ==NULL) {
            return query();
        }
        try {
            return deserializeQuery(query);
        } catch (Exception e) {
            throw new BadRequestException("query 参数错误", e);
        }
    }

    public FieldPolicy getFieldPolicy() {
        if (fieldPolicy == null || fieldPolicy==NULL) {
            return includeAll();
        }
        //我们需要去掉表达式 防止客户端 sql 注入
        try {
            return deserializeFieldPolicy(fieldPolicy);
        } catch (DeserializationException e) {
            throw new BadRequestException("fieldPolicy 参数错误", e);
        }
    }

}
