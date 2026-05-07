package dev.scx.app.base;

import dev.scx.app.ScxAppContext;
import dev.scx.app.component.Component;
import dev.scx.data.Aggregator;
import dev.scx.data.Finder;
import dev.scx.data.Repository;
import dev.scx.data.aggregation.Aggregation;
import dev.scx.data.field_policy.FieldPolicy;
import dev.scx.data.query.Query;
import dev.scx.data.sql.SQLRepository;
import dev.scx.sql.SQL;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static dev.scx.app.ScxAppHelper.findBaseModelServiceEntityClass;
import static dev.scx.data.field_policy.FieldPolicyBuilder.includeAll;
import static dev.scx.data.query.QueryBuilder.*;

/// 提供一些针对 BaseModel 类型实体类 简单的 CRUD 操作的 service 类
/// 业务 service 可以继承此类 (注意 : 如需要被 beanFactory 扫描到 请标注 [Component] 注解)
///
/// 或手动创建 : new BaseModelService()
///
/// 如果还是无法满足需求, 可以考虑使用 [SQLRunner]
///
/// @author scx567888
/// @version 0.0.1
public class BaseModelService<Entity extends BaseModel> {

    /// BaseDao
    protected final Class<Entity> entityClass;

    private SQLRepository<Entity> dao;

    /// 从泛型中获取 entityClass
    public BaseModelService() {
        this.entityClass = findBaseModelServiceEntityClass(this.getClass());
    }

    /// 手动创建 entityClass
    ///
    /// @param entityClass 继承自 [BaseModel] 的实体类 class
    public BaseModelService(Class<Entity> entityClass) {
        this.entityClass = entityClass;
    }

    /// 处理 updateFilter  使在插入或更新数据时永远过滤 "id", "dateCreated", "dateUpdated" 三个字段
    ///
    /// @param updateFilter u
    /// @return a [FieldPolicy] object
    private static FieldPolicy updateFilterProcessor(FieldPolicy updateFilter) {
        return updateFilter.exclude("id", "createdDate", "updatedDate");
    }

    /// 插入数据 (注意 !!! 这里会在插入之后根据主键再次进行一次查询, 若只是进行插入且对性能有要求请使用 {@link Repository#add(Object)})
    ///
    /// @param entity 待插入的数据
    /// @return 插入成功的数据 如果插入失败或数据没有主键则返回 null
    public final Entity add(Entity entity) {
        return add(entity, includeAll());
    }

    /// 纯表达式插入
    public final Entity add(FieldPolicy updateFilter) {
        return add((Entity) null, updateFilter);
    }

    /// 插入数据 (注意 !!! 这里会在插入之后根据主键再次进行一次查询, 若只是进行插入且对性能有要求请使用 {@link Repository#add(Object, FieldPolicy)} )})
    ///
    /// @param entity       待插入的数据
    /// @param updateFilter 更新字段过滤器
    /// @return 插入成功的数据 如果插入失败或数据没有主键则返回 null
    public Entity add(Entity entity, FieldPolicy updateFilter) {
        var newID = dao().add(entity, updateFilterProcessor(updateFilter));
        return newID != null ? this.get(newID) : null;
    }

    /// 批量插入数据
    ///
    /// @param entityList 数据集合
    /// @return 插入成功的数据的自增主键列表
    public final List<Long> add(Collection<Entity> entityList) {
        //此处没有设置 f
        return add(entityList, includeAll());
    }

    /// 批量插入数据
    ///
    /// @param entityList   数据集合
    /// @param updateFilter 更新字段过滤器
    /// @return 插入成功的数据的自增主键列表
    public List<Long> add(Collection<Entity> entityList, FieldPolicy updateFilter) {
        return dao().add(entityList, updateFilterProcessor(updateFilter));
    }

    /// 获取所有数据
    ///
    /// @return 所有数据
    public final List<Entity> find() {
        return find(query(), includeAll());
    }

    /// 获取所有数据 (使用查询过滤器)
    ///
    /// @param selectFilter 查询字段过滤器
    /// @return 所有数据
    public final List<Entity> find(FieldPolicy selectFilter) {
        return find(query(), selectFilter);
    }

    /// 根据聚合查询条件 [Query] 获取数据列表
    ///
    /// @param query 聚合查询参数对象
    /// @return 数据列表
    public final List<Entity> find(Query query) {
        return find(query, includeAll());
    }

    /// 根据聚合查询条件 [Query] 获取数据列表
    ///
    /// @param query        聚合查询参数对象
    /// @param selectFilter 查询字段过滤器
    /// @return 数据列表
    public List<Entity> find(Query query, FieldPolicy selectFilter) {
        return dao().find(query, selectFilter);
    }

    /// 根据 id 获取数据
    ///
    /// @param ids id 列表
    /// @return 列表数据
    public final List<Entity> find(long... ids) {
        return find(ids.length == 1 ? eq("id", ids[0]) : in("id", ids));
    }

    /// 根据 ID (主键) 查询单条数据
    ///
    /// @param id id ( 主键 )
    /// @return 查到多个则返回第一个 没有则返回 null
    public final Entity get(long id) {
        return get(id, includeAll());
    }

    /// 根据 ID (主键) 查询单条数据
    ///
    /// @param id           id ( 主键 )
    /// @param selectFilter 查询字段过滤器
    /// @return 查到多个则返回第一个 没有则返回 null
    public final Entity get(long id, FieldPolicy selectFilter) {
        return get(eq("id", id), selectFilter);
    }

    /// 根据聚合查询条件 [Query] 获取单条数据
    ///
    /// @param query 聚合查询参数对象
    /// @return 查到多个则返回第一个 没有则返回 null
    public final Entity get(Query query) {
        return get(query, includeAll());
    }

    /// 根据聚合查询条件 [Query] 获取单条数据
    ///
    /// @param query        聚合查询参数对象
    /// @param selectFilter 查询字段过滤器
    /// @return 查到多个则返回第一个 没有则返回 null
    public Entity get(Query query, FieldPolicy selectFilter) {
        return dao().findFirst(query, selectFilter);
    }

    /// 获取所有数据
    ///
    /// @return 所有数据
    public final Finder<Entity> finder() {
        return finder(query(), includeAll());
    }

    /// 获取所有数据 (使用查询过滤器)
    ///
    /// @param selectFilter 查询字段过滤器
    /// @return 所有数据
    public final Finder<Entity> finder(FieldPolicy selectFilter) {
        return finder(query(), selectFilter);
    }

    /// 根据聚合查询条件 [Query] 获取数据列表
    ///
    /// @param query 聚合查询参数对象
    /// @return 数据列表
    public final Finder<Entity> finder(Query query) {
        return finder(query, includeAll());
    }

    public Finder<Entity> finder(Query query, FieldPolicy selectFilter) {
        return dao().finder(query, selectFilter);
    }

    /// 根据 ID 更新 (注意 !!! 这里会在更新之后根据主键再次进行一次查询, 若只是进行更新且对性能有要求请使用 {@link Repository#update(Object, FieldPolicy, Query)})
    ///
    /// @param entity 待更新的数据 ( 注意: 请保证数据中 id 字段不为空 )
    /// @return 更新成功后的数据
    public final Entity update(Entity entity) {
        return update(entity, includeAll());
    }

    /// 根据 ID 更新 (注意 !!! 这里会在更新之后根据主键再次进行一次查询, 若只是进行更新且对性能有要求请使用 {@link Repository#update(Object, FieldPolicy, Query)})
    ///
    /// @param entity       待更新的数据 ( 注意: 请保证数据中 id 字段不为空 )
    /// @param updateFilter 更新字段过滤器
    /// @return 更新成功后的数据
    public final Entity update(Entity entity, FieldPolicy updateFilter) {
        if (entity.id == null) {
            throw new RuntimeException("根据 id 更新时 id 不能为空");
        }
        this.update(entity, updateFilter, eq("id", entity.id));
        return this.get(entity.id);
    }

    /// 根据指定条件更新数据
    ///
    /// @param entity 待更新的数据
    /// @param query  更新的条件
    /// @return 更新成功的数据条数
    public final long update(Entity entity, Query query) {
        return update(entity, includeAll(), query);
    }

    /// 根据 表达式更新数据
    public final long update(FieldPolicy updateFilter, Query query) {
        return update(null, updateFilter, query);
    }

    /// 根据指定条件更新数据
    ///
    /// @param entity       待更新的数据
    /// @param query        更新的条件
    /// @param updateFilter 更新字段过滤器
    /// @return 更新成功的数据条数
    public long update(Entity entity, FieldPolicy updateFilter, Query query) {
        return dao().update(entity, updateFilterProcessor(updateFilter), query);
    }

    /// 根据 ID 列表删除指定的数据
    ///
    /// @param ids 要删除的数据的 id 集合
    /// @return 删除成功的数据条数
    public final long delete(long... ids) {
        if (ids.length == 0) {
            throw new IllegalArgumentException("待删除的 ids 数量至少为 1 个");
        }
        return delete(ids.length == 1 ? eq("id", ids[0]) : in("id", ids));
    }

    /// 根据条件删除
    ///
    /// @param query 删除条件
    /// @return 被删除的数据条数
    public long delete(Query query) {
        return dao().delete(query);
    }


    /// 获取所有数据的条数
    ///
    /// @return 所有数据的条数
    public final long count() {
        return dao().count();
    }

    /// 根据聚合查询条件 [Query] 获取数据条数
    ///
    /// @param query 聚合查询参数对象
    /// @return 数据条数
    public final long count(Query query) {
        return dao().count(query);
    }

    public final Aggregator aggregator(Query beforeAggregateQuery, Aggregation aggregation, Query afterAggregateQuery) {
        return dao().aggregator(beforeAggregateQuery, aggregation, afterAggregateQuery);
    }

    public final Aggregator aggregator(Aggregation aggregation, Query afterAggregateQuery) {
        return aggregator(query(), aggregation, afterAggregateQuery);
    }

    public final Aggregator aggregator(Query beforeAggregateQuery, Aggregation aggregation) {
        return aggregator(beforeAggregateQuery, aggregation, query());
    }

    public final Aggregator aggregator(Aggregation aggregation) {
        return aggregator(query(), aggregation, query());
    }

    public final List<Map<String, Object>> aggregate(Query beforeAggregateQuery, Aggregation aggregation, Query afterAggregateQuery) {
        return aggregator(beforeAggregateQuery, aggregation, afterAggregateQuery).list();
    }

    public final List<Map<String, Object>> aggregate(Aggregation aggregation, Query afterAggregateQuery) {
        return aggregator(aggregation, afterAggregateQuery).list();
    }

    public final List<Map<String, Object>> aggregate(Query beforeAggregateQuery, Aggregation aggregation) {
        return aggregator(beforeAggregateQuery, aggregation).list();
    }

    public final List<Map<String, Object>> aggregate(Aggregation aggregation) {
        return aggregator(aggregation).list();
    }

    public final Map<String, Object> aggregateFirst(Query beforeAggregateQuery, Aggregation aggregation, Query afterAggregateQuery) {
        return aggregator(beforeAggregateQuery, aggregation, afterAggregateQuery).first();
    }

    public final Map<String, Object> aggregateFirst(Aggregation aggregation, Query afterAggregateQuery) {
        return aggregator(aggregation, afterAggregateQuery).first();
    }

    public final Map<String, Object> aggregateFirst(Query beforeAggregateQuery, Aggregation aggregation) {
        return aggregator(beforeAggregateQuery, aggregation).first();
    }

    public final Map<String, Object> aggregateFirst(Aggregation aggregation) {
        return aggregator(aggregation).first();
    }

    public final SQLRepository<Entity> dao() {
        if (dao == null) {
            this.dao = new SQLRepository<>(entityClass, ScxAppContext.jdbcContext());
        }
        return dao;
    }

    public final Class<Entity> entityClass() {
        return this.entityClass;
    }

    public final SQL buildListSQL(Query query, FieldPolicy selectFilter) {
        return dao().buildSelectSQL(query, selectFilter);
    }

    public final SQL buildGetSQL(Query query, FieldPolicy selectFilter) {
        return dao().buildSelectFirstSQL(query, selectFilter);
    }

}
