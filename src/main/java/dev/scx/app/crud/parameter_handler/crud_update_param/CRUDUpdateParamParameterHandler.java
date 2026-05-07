package dev.scx.app.crud.parameter_handler.crud_update_param;

import dev.scx.app.crud.CRUDUpdateParam;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.ScxSerialize;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;

/**
 * CRUDUpdateParamParameterHandler
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class CRUDUpdateParamParameterHandler implements ParameterHandler {

    private final ParameterInfo parameter;

    public CRUDUpdateParamParameterHandler(ParameterInfo parameter) {
        this.parameter = parameter;
    }

    @Override
    public Object handle(RequestInfo requestInfo) throws Exception {
        var javaType = parameter.parameterType();
        var name = parameter.name();
        var required = false;
        var useAllBody = true;
        CRUDUpdateParam crudUpdateParam = ScxSerialize.nodeToObject(requestInfo.body(), CRUDUpdateParam.class);
        //这里保证 方法上的 CRUDUpdateParam 类型参数永远不为空
        return crudUpdateParam != null ? crudUpdateParam : new CRUDUpdateParam();
    }

}
