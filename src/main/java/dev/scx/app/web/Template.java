package dev.scx.app.web;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.media_type.ScxMediaType;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static dev.scx.http.media_type.MediaType.TEXT_HTML;
import static java.nio.charset.StandardCharsets.UTF_8;

/// Template
///
/// @author scx567888
/// @version 0.0.1
public final class Template  {

    private final String templatePath;
    private final Map<String, Object> dataMap;

    private Template(String templatePath) {
        this.templatePath = templatePath;
        this.dataMap = new HashMap<>();
    }

    public static Template of(String templatePath) {
        return new Template(templatePath);
    }

    public Template add(String key, Object value) {
        dataMap.put(key, value);
        return this;
    }


    public void apply(ScxHttpServerRequest request, TemplateEngine templateHandler) throws Exception {
        if (templateHandler == null) {
            throw new IllegalStateException("No templateHandler configured in WebContext");
        }

        var sw = new StringWriter();
        var template = templateHandler.getTemplate(templatePath);
        template.process(dataMap, sw);

        request.response()
            .contentType(ScxMediaType.of(TEXT_HTML).charset(UTF_8))
            .send(sw.toString());
    }

}
