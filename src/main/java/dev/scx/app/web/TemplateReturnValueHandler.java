package dev.scx.app.web;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.web.ScxWeb;
import dev.scx.web.return_value_handler.ReturnValueHandler;

/// TemplateReturnValueHandler
///
/// @author scx567888
/// @version 0.0.1
public final class TemplateReturnValueHandler implements ReturnValueHandler {

    private TemplateEngine templateEngine;

    public TemplateReturnValueHandler(TemplateEngine templateEngine) {
        this.templateEngine =templateEngine;
    }

    public TemplateEngine templateEngine() {
        return templateEngine;
    }

    public TemplateReturnValueHandler templateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        return this;
    }

    @Override
    public boolean canHandle(Object returnValue) {
        return returnValue instanceof Template;
    }

    @Override
    public void handle(Object returnValue, ScxHttpServerRequest request, ScxWeb scxWeb) throws Exception {
        if (!(returnValue instanceof Template template)) {
            throw new IllegalArgumentException("参数不是 Template 类型 !!! " + returnValue.getClass());
        }
        template.apply(request, templateEngine);
    }

}
