package dev.scx.app.config;

import dev.scx.ansi.Ansi;
import dev.scx.node.Node;
import dev.scx.node.ObjectNode;
import dev.scx.reflect.TypeReference;
import dev.scx.serialize.ScxSerialize;

import java.io.File;
import java.nio.file.Path;

final class ScxConfigImpl implements ScxConfig {

    private final File jsonFile;
    private final ObjectNode value;

    ScxConfigImpl(File jsonFile) {
        this.jsonFile = jsonFile;
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

    @Override
    public Node get(String path) {
        return null;
    }

    @Override
    public <T> T get(String path, Class<T> type) {
        return null;
    }

    @Override
    public <T> T get(String path, TypeReference<T> type) {
        return null;
    }

    @Override
    public <T> T getOrDefault(String path, Class<T> type, T defaultValue) {
        return null;
    }

    @Override
    public <T> T getOrDefault(String path, TypeReference<T> type, T defaultValue) {
        return null;
    }


    private static class JsonConfigFileNotObjectException extends Exception {

        public JsonConfigFileNotObjectException(File jsonFile) {
            super("N 配置文件必须为 Object 格式!!! 请确保配置文件格式正确 : " + jsonFile);
        }

    }

}
