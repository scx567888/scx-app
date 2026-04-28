package dev.scx.app.web;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.Version;

import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Path;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

/// 模板引擎
///
/// @author scx567888
/// @version 0.0.1
public final class TemplateEngine {

    private static final Logger logger = System.getLogger(TemplateEngine.class.getName());

    private static final Version VERSION = Configuration.VERSION_2_3_31;

    private final Configuration freemarkerConfig = new Configuration(VERSION);

    public TemplateEngine(Path templateRoot) {
        // freemarker 配置文件版本
        var wrapperBuilder = new DefaultObjectWrapperBuilder(VERSION);
        //暴露 实体类的 fields 因为 此项目中的实体类没有 get set
        wrapperBuilder.setExposeFields(true);
        freemarkerConfig.setObjectWrapper(wrapperBuilder.build());
        try {
            freemarkerConfig.setDirectoryForTemplateLoading(templateRoot.toFile());
        } catch (Exception e) {
            logger.log(INFO, "模板目录不存在!!! {0}", templateRoot);
        }

        //设置 字符集
        freemarkerConfig.setDefaultEncoding("UTF-8");
        //设置 语法 为自动检测
        freemarkerConfig.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
    }

    public Template getTemplate(String pagePath) throws IOException {
        return freemarkerConfig.getTemplate(pagePath);
    }

    public void addDirective(BaseTemplateDirective myDirective) {
        logger.log(DEBUG, "已添加自定义 Freemarker 标签 [{0}] Class -> {1}", myDirective.directiveName(), myDirective.getClass().getName());
        freemarkerConfig.setSharedVariable(myDirective.directiveName(), myDirective);
    }

}
