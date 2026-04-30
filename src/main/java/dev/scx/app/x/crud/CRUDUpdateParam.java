package dev.scx.app.x.crud;

import dev.scx.app.base.BaseModel;
import dev.scx.data.field_policy.FieldPolicy;
import dev.scx.data.sql.schema_mapping.EntityTable;

import java.util.Arrays;
import java.util.Map;

import static dev.scx.data.field_policy.FieldPolicyBuilder.include;
import static dev.scx.data.field_policy.FieldPolicyBuilder.includeAll;

/**
 * 更新实体类的封装
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class CRUDUpdateParam {

    /**
     * 更新的所有内容 可以转换为对应的 实体类
     */
    public Map<String, Object> updateModel;

    /**
     * 需要更新的字段列
     */
    public String[] needUpdateFieldNames;

    public <B extends BaseModel> B getBaseModel(Class<B> modelClass) {
        return CRUDHelper.mapToBaseModel(this.updateModel, modelClass);
    }

    public FieldPolicy getUpdatePolicy(Class<? extends BaseModel> modelClass, EntityTable<?> scxDaoTableInfo) {
        if (needUpdateFieldNames == null) {
            return includeAll();
        }
        var legalFieldName = Arrays.stream(needUpdateFieldNames).map(fieldName -> CRUDHelper.checkFieldName(modelClass, fieldName)).toArray(String[]::new);
        return include(legalFieldName).ignoreNull(false);
    }

}
