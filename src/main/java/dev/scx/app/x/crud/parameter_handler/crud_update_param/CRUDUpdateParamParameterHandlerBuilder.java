package dev.scx.app.x.crud.parameter_handler.crud_update_param;

import dev.scx.app.x.crud.CRUDUpdateParam;
import dev.scx.reflect.ParameterInfo;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/**
 * CRUDUpdateParamParameterHandlerBuilder
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class CRUDUpdateParamParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        if (parameter.parameterType().rawClass() != CRUDUpdateParam.class) {
            return null;
        }
        return new CRUDUpdateParamParameterHandler(parameter);
    }

}
