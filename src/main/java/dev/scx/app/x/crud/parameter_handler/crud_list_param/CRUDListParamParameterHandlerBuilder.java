package dev.scx.app.x.crud.parameter_handler.crud_list_param;

import dev.scx.app.x.crud.CRUDListParam;
import dev.scx.reflect.ParameterInfo;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/**
 * CRUDListParamParameterHandlerBuilder
 *
 * @author scx567888
 * @version 0.0.1
 */
public class CRUDListParamParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        if (parameter.parameterType().rawClass() != CRUDListParam.class) {
            return null;
        }
        return new CRUDListParamParameterHandler(parameter);
    }

}
