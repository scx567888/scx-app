package dev.scx.app.x.crud;

import dev.scx.app.ScxApp;
import dev.scx.app.ScxAppModule;
import dev.scx.app.x.crud.parameter_handler.crud_list_param.CRUDListParamParameterHandlerBuilder;
import dev.scx.app.x.crud.parameter_handler.crud_update_param.CRUDUpdateParamParameterHandlerBuilder;

import static java.lang.System.Logger;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.getLogger;

/**
 * 为 BaseModel 的实现类 提供一套简单的 "单表" 的增删改查 api
 *
 * @author scx567888
 * @version 0.0.1
 */
public class CRUDModule extends ScxAppModule {

    private static final Logger logger = getLogger(CRUDModule.class.getName());

    public CRUDModule() {

    }

    @Override
    public void start(ScxApp scx) {
        //这里添加额外的参数处理器 保证 CRUDListParam 类型的参数永不为空
        scx.scxWeb().addParameterHandlerBuilder(0, new CRUDListParamParameterHandlerBuilder());
        scx.scxWeb().addParameterHandlerBuilder(0, new CRUDUpdateParamParameterHandlerBuilder());
        logger.log(DEBUG, "已添加用于处理类型为 CRUDListParam   的 ParameterHandlerBuilder  -->  {0}", CRUDListParamParameterHandlerBuilder.class.getName());
        logger.log(DEBUG, "已添加用于处理类型为 CRUDUpdateParam 的 ParameterHandlerBuilder  -->  {0}", CRUDUpdateParamParameterHandlerBuilder.class.getName());
    }

    @Override
    public String name() {
        return "SCX_EXT-" + super.name();
    }

}
