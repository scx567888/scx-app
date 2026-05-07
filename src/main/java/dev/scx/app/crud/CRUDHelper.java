package dev.scx.app.crud;

import dev.scx.app.base.BaseModel;
import dev.scx.app.base.BaseModelService;
import dev.scx.app.crud.exception.UnknownFieldNameException;
import dev.scx.data.sql.annotation.NoColumn;
import dev.scx.http.exception.BadRequestException;
import dev.scx.reflect.ClassInfo;

import java.lang.System.Logger;
import java.util.Map;

import static dev.scx.reflect.ScxReflect.typeOf;
import static dev.scx.serialize.ScxSerialize.convertObject;
import static java.lang.System.Logger.Level.ERROR;

/**
 * CRUDHelper
 *
 * @author scx567888
 * @version 0.0.1
 */
public final class CRUDHelper {

    /**
     * a
     */
    private static final Logger logger = System.getLogger(CRUDHelper.class.getName());

    /**
     * 获取 baseModel
     *
     * @param map            a
     * @param baseModelClass a
     * @param <B>            b
     * @return a
     */
    public static <B extends BaseModel> B mapToBaseModel(Map<String, Object> map, Class<B> baseModelClass) {
        try {
            return convertObject(map, baseModelClass);
        } catch (Exception e) {
            logger.log(ERROR, "将 Map 转换为 BaseModel 时发生异常 : ", e);
            //这里一般就是 参数转换错误
            throw new BadRequestException(e);
        }
    }

    /**
     * 检查 fieldName 是否合法
     *
     * @param modelClass m
     * @param fieldName  f
     * @return a {@link String} object
     * @throws UnknownFieldNameException c
     */
    public static String checkFieldName(Class<?> modelClass, String fieldName) throws UnknownFieldNameException {
        try {
            var field = modelClass.getField(fieldName);
            if (field.isAnnotationPresent(NoColumn.class)) {
                throw new UnknownFieldNameException(fieldName);
            }
        } catch (Exception e) {
            throw new UnknownFieldNameException(fieldName);
        }
        return fieldName;
    }

    @SuppressWarnings("unchecked")
    static Class<? extends BaseModelService<?>> findBaseModelServiceClass(Class<?> baseCRUDControllerClass) {
        var superClass = ((ClassInfo) typeOf(baseCRUDControllerClass)).findSuperType(BaseCRUDController.class);
        if (superClass != null) {
            var boundType = superClass.bindings().get(0);
            if (boundType != null) {
                return (Class<? extends BaseModelService<?>>) boundType.rawClass();
            } else {
                throw new IllegalArgumentException(baseCRUDControllerClass.getName() + " : 必须设置泛型参数 !!!");
            }
        } else {
            throw new IllegalArgumentException(baseCRUDControllerClass.getName() + " : 必须继承自 BaseCRUDController !!!");
        }
    }

}
