package dev.scx.app.web;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.web.ScxWeb;
import dev.scx.web.return_value_handler.ReturnValueHandler;

public class TemplateReturnValueHandler implements ReturnValueHandler {

    private TemplateEngine templateHandler;

    public TemplateReturnValueHandler(TemplateEngine templateHandler) {
        this.templateHandler=templateHandler;
    }

    public TemplateEngine templateHandler() {
        return templateHandler;
    }

    public TemplateReturnValueHandler setTemplateHandler(TemplateEngine templateHandler) {
        this.templateHandler = templateHandler;
        return this;
    }

    @Override
    public boolean canHandle(Object returnValue) {
        return returnValue instanceof Template;
    }

    @Override
    public void handle(Object returnValue, ScxHttpServerRequest request, ScxWeb scxWeb) throws Exception {
        if (returnValue instanceof Template template){
            template.apply(request,templateHandler);
            return;
        }
        throw new IllegalArgumentException("");
    }
}
