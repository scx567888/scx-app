package dev.scx.app.config;

import dev.scx.ansi.Ansi;
import dev.scx.node.Node;
import dev.scx.node.ObjectNode;
import dev.scx.object.x.DefaultObjectNodeConvertOptions;
import dev.scx.object.x.DefaultObjectNodeConverter;
import dev.scx.reflect.TypeReference;
import dev.scx.serialize.ScxSerialize;

import java.io.File;

import static dev.scx.reflect.ScxReflect.typeOf;

final class ScxConfigImpl implements ScxConfig {

    private static final DefaultObjectNodeConverter CONFIG_OBJECT_NODE_CONVERTER=DefaultObjectNodeConverter.builder()
        .registerDefaultMappers()
        .registerMapper(new ConfiguredPathNodeMapper())
        .registerMapper(new ScxPasswordNodeMapper())
        .build();

    private static final DefaultObjectNodeConvertOptions CONFIG_OBJECT_NODE_CONVERT_OPTIONS=new DefaultObjectNodeConvertOptions();


    private final File jsonFile;
    private final ScxEnvironment scxEnvironment;
    private final ObjectNode value;

    ScxConfigImpl(File jsonFile, ScxEnvironment scxEnvironment) {
        this.jsonFile = jsonFile;
        this.scxEnvironment = scxEnvironment;
        this.value = loadFromJsonFile(jsonFile);
    }

    public static ObjectNode loadFromJsonFile(File jsonFile) {
        try {
            var value = ScxSerialize.fromJson(jsonFile);
            if (value instanceof ObjectNode objectNode) {
                Ansi.ansi().brightBlue("Y 已加载配置文件 : " + jsonFile).println();
                return objectNode;
            } else {
                throw new JsonConfigFileNotObjectException(jsonFile);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("配置文件加载失败 !!!", e);
        }
    }

    public static Node getByPath(Node node, String path) {
        var paths = path.split("\\.");
        var result = node;
        for (var p : paths) {
            if (result != null) {
                if (result instanceof ObjectNode i) {
                    result = i.get(p);
                } else {
                    result = null;
                }
            } else {
                break;
            }
        }
        return result;
    }

    @Override
    public Node get(String path) {
        return getByPath(value, path);
    }

    @Override
    public <T> T get(String path, Class<T> type) {
        Node node = get(path);
        return CONFIG_OBJECT_NODE_CONVERTER.nodeToObject(node, type, CONFIG_OBJECT_NODE_CONVERT_OPTIONS);
    }

    @Override
    public <T> T get(String path, TypeReference<T> type) {
        Node node = get(path);
        return CONFIG_OBJECT_NODE_CONVERTER.nodeToObject(node, typeOf(type), CONFIG_OBJECT_NODE_CONVERT_OPTIONS);
    }

    @Override
    public <T> T getOrDefault(String path, Class<T> type, T defaultValue) {
        Node node = get(path);
        if (node == null) {
            return defaultValue;
        }
        return CONFIG_OBJECT_NODE_CONVERTER.nodeToObject(node, type, CONFIG_OBJECT_NODE_CONVERT_OPTIONS);
    }

    @Override
    public <T> T getOrDefault(String path, TypeReference<T> type, T defaultValue) {
        Node node = get(path);
        if (node == null) {
            return defaultValue;
        }
        return CONFIG_OBJECT_NODE_CONVERTER.nodeToObject(node,typeOf(type) , CONFIG_OBJECT_NODE_CONVERT_OPTIONS);
    }

    private static class JsonConfigFileNotObjectException extends Exception {

        public JsonConfigFileNotObjectException(File jsonFile) {
            super("N 配置文件必须为 Object 格式!!! 请确保配置文件格式正确 : " + jsonFile);
        }

    }

}
