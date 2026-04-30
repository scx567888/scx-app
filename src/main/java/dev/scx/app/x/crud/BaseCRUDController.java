package dev.scx.app.x.crud;



import dev.scx.app.base.BaseModelService;
import dev.scx.app.web.Result;
import dev.scx.web.annotation.Body;
import dev.scx.web.annotation.BodyField;
import dev.scx.web.annotation.PathCapture;
import dev.scx.web.annotation.Route;
import dev.scx.web.result.WebResult;

import java.util.Map;

import static dev.scx.app.ScxAppContext.getBean;
import static dev.scx.app.x.crud.CRUDHelper.findBaseModelServiceClass;
import static dev.scx.data.query.BuildControl.SKIP_IF_NULL;
import static dev.scx.data.query.QueryBuilder.and;
import static dev.scx.http.method.HttpMethod.*;


/**
 * 继承方式的 CRUD 的 controller
 *
 * @author scx567888
 * @version 0.0.1
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BaseCRUDController<T extends BaseModelService> {

    protected final T service;

    public BaseCRUDController(T service) {
        this.service = service;
    }

    public BaseCRUDController() {
        this.service = getBean((Class<T>) findBaseModelServiceClass(this.getClass()));
    }

    @Route(methods = POST)
    public WebResult list(CRUDListParam crudListParam) {
        var query = crudListParam.getQuery();
        var selectFilter = crudListParam.getFieldPolicy();
        var list = service.find(query, selectFilter);
        var total = service.count(query);
        return Result.ok().put("items", list).put("total", total);
    }

    @Route(value = ":id", methods = GET)
    public WebResult info(@PathCapture Long id) {
        var info = service.get(id);
        return Result.ok(info);
    }

    @Route(value = "", methods = POST)
    public WebResult add(@Body Map<String, Object> saveModel) {
        var realObject = CRUDHelper.mapToBaseModel(saveModel, service.entityClass());
        var savedModel = service.add(realObject);
        return Result.ok(savedModel);
    }

    @Route(value = "", methods = PUT)
    public WebResult update(CRUDUpdateParam crudUpdateParam) {
        var realObject = crudUpdateParam.getBaseModel(service.entityClass());
        var updatePolicy = crudUpdateParam.getUpdatePolicy(service.entityClass(), service.dao().table());
        var updatedModel = service.update(realObject, updatePolicy);
        return Result.ok(updatedModel);
    }

    @Route(value = "", methods = DELETE)
    public WebResult delete(CRUDListParam crudListParam) {
        var query = crudListParam.getQuery();
        var size = service.delete(query);
        return Result.ok(size);
    }

    @Route(value = "check-unique/:fieldName", methods = POST)
    public WebResult checkUnique(@PathCapture String fieldName, @BodyField Object value, @BodyField(required = false) Long id) {
        CRUDHelper.checkFieldName(service.entityClass(), fieldName);
        var query = and().eq(fieldName, value).ne("id", id, SKIP_IF_NULL);
        var isUnique = service.count(query) == 0;
        return Result.ok().put("isUnique", isUnique);
    }

    @Route(methods = POST)
    public WebResult count(CRUDListParam crudListParam) {
        var query = crudListParam.getQuery();
        var total = service.count(query);
        return Result.ok(total);
    }

}
