package dev.scx.app.crud.parameter_handler.crud_list_param;

import dev.scx.app.crud.CRUDListParam;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.ScxSerialize;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;

/**
 * CRUDListParamParameterHandler
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class CRUDListParamParameterHandler implements ParameterHandler {

    private final ParameterInfo parameter;

    public CRUDListParamParameterHandler(ParameterInfo parameter) {
        this.parameter = parameter;
    }

    @Override
    public Object handle(RequestInfo requestInfo) throws Exception {
        var javaType = parameter.parameterType();
        var name = parameter.name();
        var required = false;
        var useAllBody = true;

        CRUDListParam crudListParam = ScxSerialize.nodeToObject(requestInfo.body(), CRUDListParam.class);
        //这里保证 方法上的 CRUDListParam 类型参数永远不为空
        return crudListParam != null ? crudListParam : new CRUDListParam();
    }

}
