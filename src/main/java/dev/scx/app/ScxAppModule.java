package dev.scx.app;

/// ScxApp 的模块.
///
/// 一个 ScxAppModule 表示 ScxApp 启动流程中的一个动作单元.
///
/// ScxAppModule 的完整执行顺序如下:
///
/// 1. ScxApp 调用所有模块的 `init(ScxAppInitContext)`
///
///    每个模块根据当前配置和初始化上下文创建自身所需的对象,
///    并返回 ScxAppModuleDefinition.
///
/// 2. ScxApp 汇总所有 ScxAppModuleDefinition
///
///    ScxApp 会根据所有模块返回的定义:
///
///    - 收集组件候选类
///    - 收集组件选择器
///    - 收集组件实例
///    - 构建并初始化 DI 容器
///    - 根据 startBefore / startAfter 计算模块 start 顺序
///
/// 3. ScxApp 按计算出的顺序调用所有模块的 `start(ScxApp)`
///
///    此时 DI 容器已经完成构建,
///    模块可以通过 ScxApp 访问应用运行时能力.
///
/// 4. ScxApp 在停止应用时, 按成功 start 的反向顺序调用 `stop(ScxApp)`.
///
/// ## start 的含义
///
/// start 的含义是“执行当前模块在启动流程中的动作”.
///
/// 这个动作不一定是启动外部资源.
///
/// 例如:
///
/// - Web 模块的 start 可以是扫描 Controller 并注册路由;
/// - CORS 模块的 start 可以是注册 CORS route 或 filter;
/// - Auth 模块的 start 可以是注册认证 filter 或 route guard;
/// - StaticServer 模块的 start 可以是注册静态资源 route;
/// - SQL 修表模块的 start 可以是根据 SQLClient 修复表结构;
/// - HTTP 模块的 start 可以是真正监听端口;
/// - Scheduler 模块的 start 可以是启动调度器;
/// - ServiceDiscovery 模块的 start 可以是在 HTTP 启动后注册服务发现.
///
/// 因此, 不要把 start 狭义理解成:
///
/// ```
/// 启动服务器 / 打开端口 / 启动线程
/// ```
///
/// 它在 ScxAppModule 中表达的是:
///
/// ```
/// 当前模块作为启动图节点时需要执行的动作
/// ```
///
/// ## 核心设计判断
///
/// ScxAppModule 不是“生命周期对象”, 而是“启动动作节点”.
///
/// 这是本接口最重要的设计约束.
///
/// 如果把 ScxAppModule 理解成生命周期对象,
/// 那么每遇到一个新的启动时机, 都会自然想增加一个新的生命周期方法:
///
/// ```
/// prepare()
/// start()
/// started()
/// ready()
/// afterReady()
/// beforeStop()
/// destroy()
/// ```
///
/// 这条路的问题不在于“阶段数量不够”.
///
/// 问题在于:
///
/// ```
/// 它用全局阶段表达局部顺序.
/// ```
///
/// 启动流程中的很多需求, 本质上只是动作之间的先后关系:
///
/// ```
/// Web 路由注册 必须早于 HTTP listen
/// CORS 注册 必须早于 HTTP listen
/// Auth filter 注册 必须早于 HTTP listen
/// 静态资源 route 注册 必须早于 HTTP listen
/// SQL 修表 必须晚于 SQLClient 创建
/// HTTP listen 必须晚于 SQL 修表
/// 服务发现注册 必须晚于 HTTP listen
/// 用户 runner 必须晚于基础设施启动
/// ```
///
/// 这些需求的本质不是:
///
/// ```
/// ScxAppModule 需要更多生命周期阶段
/// ```
///
/// 而是:
///
/// ```
/// 启动动作之间存在有向依赖关系
/// ```
///
/// 所以 ScxApp 的规则是:
///
/// ```
/// 新时机 != 新生命周期方法
/// 新时机 = 新模块节点 + startBefore / startAfter
/// ```
///
/// 例如:
///
/// ```
/// WebRouteModule.startBefore(HttpModule)
/// CorsModule.startBefore(HttpModule)
/// AuthModule.startBefore(HttpModule)
/// StaticServerModule.startBefore(HttpModule)
/// SqlRepairModule.startAfter(SqlModule).startBefore(HttpModule)
/// ServiceDiscoveryModule.startAfter(HttpModule)
/// UserRunnerModule.startAfter(HttpModule)
/// ```
///
/// 这些模块都只有一个 start,
/// 但它们通过 startBefore / startAfter 组成了完整的启动图.
///
/// ## 为什么不要用 4 个、5 个甚至更多生命周期阶段解决问题?
///
/// 很容易从一个真实问题开始走向多阶段生命周期模型.
///
/// 例如, 最开始只有:
///
/// ```
/// start()
/// ```
///
/// 然后遇到问题:
///
/// ```
/// HttpModule.start 监听端口太早;
/// WebModule 还没来得及注册 Controller 路由.
/// ```
///
/// 于是看起来可以增加一个阶段:
///
/// ```
/// prepare()
/// start()
/// ```
///
/// 让:
///
/// ```
/// WebModule.prepare  注册路由
/// HttpModule.start    监听端口
/// ```
///
/// 这确实能解决当前问题.
///
/// 但是这个方案并没有真正解决“启动动作之间如何表达顺序”的问题.
/// 它只是把这一次遇到的顺序关系:
///
/// ```
/// 注册路由 必须早于 HTTP listen
/// ```
///
/// 固化成了一个新的全局阶段:
///
/// ```
/// prepare 必须早于 start
/// ```
///
/// 很快又会出现新的时机需求:
///
/// ```
/// 服务发现注册 必须晚于 HTTP listen
/// 启动完成日志 必须晚于所有服务启动
/// SQL 修表 必须晚于 SQLClient 创建
/// HTTP listen 必须晚于 SQL 修表
/// Scheduler 启动 必须晚于 Job 注册
/// Scheduler 启动后 还要执行一次检查
/// ```
///
/// 如果继续用增加阶段来解决, 就会自然得到:
///
/// ```
/// prepare()
/// start()
/// started()
/// ready()
/// afterReady()
/// beforeSchedulerStart()
/// afterSchedulerStart()
/// ...
/// ```
///
/// 这说明问题不在于阶段数量不够.
///
/// 4 个阶段不够时, 增加到 5 个阶段并不会从根本上解决问题.
/// 5 个阶段不够时, 还可以继续增加到 10 个、100 个.
///
/// 但这些阶段本质上都只是在给:
///
/// ```
/// 某个动作之前
/// 某个动作之后
/// ```
///
/// 命名.
///
/// 换句话说, 多阶段生命周期模型会把:
///
/// ```
/// A 必须在 B 之前执行
/// C 必须在 D 之后执行
/// E 必须在 A 和 C 之后执行
/// ```
///
/// 这种局部顺序关系, 强行提升成:
///
/// ```
/// 所有模块都必须经过的全局阶段
/// ```
///
/// 这会带来两个问题.
///
/// 第一, 全局阶段会不断膨胀.
///
/// 每当出现一个新的启动时机需求,
/// 都可能诱导出一个新的生命周期方法.
///
/// 第二, 阶段名称会越来越不像抽象模型,
/// 而像具体场景的补丁.
///
/// 例如:
///
/// ```
/// started
/// ready
/// afterReady
/// beforeSchedulerStart
/// afterSchedulerStart
/// ```
///
/// 这些阶段本质上并不是所有模块都天然拥有的生命周期,
/// 而只是某些动作之间的局部顺序需求.
///
/// 因此, “增加第 4 个阶段” 和 “增加第 5 个、第 100 个阶段”
/// 在本质上没有区别.
///
/// 它们都没有直接表达真正的问题:
///
/// ```
/// 启动动作之间存在有向依赖关系.
/// ```
///
/// ScxApp 在这里选择的模型是:
///
/// ```
/// 模块 = 启动动作节点
/// startBefore / startAfter = 节点之间的顺序边
/// ScxApp = 启动图执行器
/// ```
///
/// 因此, 启动流程的扩展方式不是继续增加全局阶段,
/// 而是增加新的动作节点和顺序边.
///
/// ## 多个启动时机应该如何表达?
///
/// 如果某个功能确实需要在多个不同时间点执行动作,
/// 不应该继续给 ScxAppModule 增加新的生命周期方法.
///
/// 推荐做法是拆成多个 ScxAppModule.
///
/// 例如, 一个管理功能可能同时需要:
///
/// 1. 在 HTTP Server 启动前注册 `/health` 路由;
/// 2. 在 HTTP Server 启动后注册服务发现.
///
/// 不推荐写成:
///
/// ```java
/// class AdminModule implements ScxAppModule {
///
///     void prepare(...) {
///         // 注册 /health
///     }
///
///     void started(...) {
///         // 注册服务发现
///     }
///
/// }
/// ```
///
/// 推荐拆成:
///
/// ```java
/// class HealthRouteModule implements ScxAppModule {
///
///     @Override
///     public ScxAppModuleDefinition init(ScxAppInitContext context) {
///         return ScxAppModuleDefinition.of()
///             .startBefore(ScxAppHttpModule.class);
///     }
///
///     @Override
///     public void start(ScxApp app) {
///         // 注册 /health route
///     }
///
/// }
/// ```
///
/// ```java
/// class ServiceDiscoveryModule implements ScxAppModule {
///
///     @Override
///     public ScxAppModuleDefinition init(ScxAppInitContext context) {
///         return ScxAppModuleDefinition.of()
///             .startAfter(ScxAppHttpModule.class);
///     }
///
///     @Override
///     public void start(ScxApp app) {
///         // 注册服务发现
///     }
///
/// }
/// ```
///
/// 这样每个 ScxAppModule 都只表示一个清晰的启动动作.
/// 启动时机由 startBefore / startAfter 表达,
/// 而不是通过不断增加全局阶段表达.
///
/// 这不是把一个模块“拆碎”,
/// 而是把原本混在一起的多个启动动作显式建模.
///
/// 一个功能可以由多个 ScxAppModule 组成;
/// 一个 ScxAppModule 应该只承担一个明确的启动时机.
///
/// ## 为什么不直接暴露 Action API?
///
/// 从更底层的角度看,
/// ScxAppModule 确实可以被理解为一个 action 节点.
///
/// 但是 ScxApp 仍然保留 ScxAppModule 这个概念,
/// 而不是直接暴露裸的 Action DAG API.
///
/// 原因是:
///
/// - Module 这个概念更适合用户理解;
/// - Module 可以同时声明组件候选类、组件选择器、组件实例和启动顺序;
/// - Module 可以持有自己的成员对象;
/// - Module 可以拥有 stop, 用来撤销 start 中建立的行为或资源;
/// - 对大多数应用集成场景来说, Module 比裸 Action 更自然.
///
/// 因此, ScxAppModule 是一个折中抽象:
///
/// ```
/// 它本质上是启动图节点,
/// 但 API 形态上仍然是模块.
/// ```
///
/// ## 不要改回传统多阶段生命周期模型
///
/// 未来如果发现某个场景用现有阶段“不好放”,
/// 不要先问:
///
/// ```
/// 是不是应该新增一个生命周期阶段?
/// ```
///
/// 而应该先问:
///
/// ```
/// 这是不是一个独立启动动作?
/// 它应该在谁之前执行?
/// 它应该在谁之后执行?
/// ```
///
/// 如果答案可以用一个新的 ScxAppModule 和 startBefore / startAfter 表达,
/// 那就不应该新增全局阶段.
///
/// 未来如果遇到某个模块需要“在某个动作之前”或“在某个动作之后”执行,
/// 优先考虑:
///
/// ```
/// 1. 是否应该用 startBefore / startAfter 表达顺序关系;
/// 2. 是否应该把一个过大的模块拆成多个 ScxAppModule;
/// 3. 是否应该新增一个更小、更明确的模块节点.
/// ```
///
/// 不要轻易增加:
///
/// ```
/// prepare()
/// started()
/// ready()
/// afterReady()
/// beforeStart()
/// afterStart()
/// ```
///
/// 这些方法不是不能实现功能,
/// 而是会把局部顺序关系重新变成全局阶段,
/// 使 ScxAppModule 从“启动图节点”退回“复杂生命周期对象”.
///
/// ScxApp 的设计目标是:
///
/// ```
/// 用模块拆分表达动作边界;
/// 用 startBefore / startAfter 表达动作顺序;
/// 用 init / start / stop 保持模块 API 稳定.
/// ```
///
/// @author scx567888
/// @version 0.0.1
public interface ScxAppModule {

    /// 初始化当前模块, 并返回当前模块在本次 ScxApp 中的定义.
    ///
    /// 该方法会在 DI 容器构建之前被调用.
    /// ScxApp 会收集所有模块返回的 ScxAppModuleDefinition,
    /// 并在之后统一构建 DI 容器和模块启动顺序.
    ///
    /// @param context 当前 ScxApp 的初始化上下文
    /// @return 当前模块的定义信息
    default ScxAppModuleDefinition init(ScxAppInitContext context) {
        return ScxAppModuleDefinition.of();
    }

    /// 执行当前模块的启动动作.
    ///
    /// 该方法会在所有模块 init 完成, 且 ScxApp 完成 DI 容器构建之后调用.
    /// ScxApp 会根据各模块定义中的 startBefore / startAfter 计算调用顺序.
    ///
    /// @param scxApp 当前 ScxApp 实例
    default void start(ScxApp scxApp) {

    }

    /// 停止当前模块.
    ///
    /// ScxApp 会按成功 start 的反向顺序调用该方法.
    ///
    /// @param scxApp 当前 ScxApp 实例
    default void stop(ScxApp scxApp) {

    }

}
