package dev.scx.app.web;

import freemarker.core.Environment;
import freemarker.template.*;
import freemarker.template.utility.DeepUnwrap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/// Freemarker 标签父类
///
/// 让子类实现后, 利用模版设计模式, 委派给子类
///
/// @author scx567888
/// @version 0.0.1
public interface BaseTemplateDirective extends TemplateDirectiveModel {

    /// 格式化参数
    ///
    /// @param params p
    /// @return p
    @SuppressWarnings("unchecked")
    static Map<String, Object> formatMapParams(Map<?, ?> params) throws TemplateModelException {
        var newMap = new HashMap<String, Object>();
        for (var s : ((Map<String, TemplateModel>) params).entrySet()) {
            newMap.put(s.getKey(), DeepUnwrap.unwrap(s.getValue()));
        }
        return newMap;
    }

    @Override
    default void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        // 获取原始结果
        var results = handle(formatMapParams(params));
        // 将结果进行处理 , 包装为 TemplateModel
        var wrap = env.getObjectWrapper().wrap(results);
        // 设置到环境变量中
        env.setVariable(variableName(), wrap);
        // 渲染输出
        body.render(env.getOut());
    }

    /// 获取自定义指令的名称
    String directiveName();

    /// 获取自定义 变量的名称
    String variableName();

    /// 委派下去让子类实现, 并且返回加工后的返回值
    /// 可返回业务对象, 或者集合
    Object handle(Map<String, Object> params);

}
