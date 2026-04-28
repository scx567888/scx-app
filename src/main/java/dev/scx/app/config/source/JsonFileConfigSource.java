package dev.scx.app.config.source;

import dev.scx.ansi.Ansi;
import dev.scx.app.config.helper.FileWatcher;
import dev.scx.format.FormatToNodeException;
import dev.scx.node.ObjectNode;
import dev.scx.serialize.ScxSerialize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/// JsonFileConfigSource
///
/// @author scx567888
/// @version 0.0.1
public final class JsonFileConfigSource extends AbstractConfigSource {

    private final Path jsonPath;
    private FileWatcher fileWatcher;

    private JsonFileConfigSource(Path jsonPath) {
        this.jsonPath = jsonPath;
        this.configMapping = loadFromJsonFile(jsonPath);
        bindOnChange(this.jsonPath);
    }

    public static ObjectNode loadFromJsonFile(Path jsonPath) {
        if (jsonPath == null) {
            throw new IllegalArgumentException("jsonPath 不能为空 !!!");
        }
        try {
            if (Files.notExists(jsonPath)) {
                throw new JsonConfigFileMissingException();
            }
            var rawMap = ScxSerialize.fromJson(jsonPath.toFile());
            if (rawMap instanceof ObjectNode objectNode) {
                Ansi.ansi().brightBlue("Y 已加载配置文件 : " + jsonPath).println();
                return objectNode;
            } else {
                throw new JsonConfigFileNotObjectException();
            }
        } catch (Exception e) {
            switch (e) {
                case JsonConfigFileMissingException _ ->
                        Ansi.ansi().red("N 配置文件已丢失!!! 请确保配置文件存在 : " + jsonPath).println();
                case FormatToNodeException _ ->
                        Ansi.ansi().red("N 配置文件已损坏!!! 请确保配置文件格式正确 : " + jsonPath).println();
                case JsonConfigFileNotObjectException _ ->
                        Ansi.ansi().red("N 配置文件必须为 Object 格式!!! 请确保配置文件格式正确 : " + jsonPath).println();
                default -> e.printStackTrace();
            }
            return new ObjectNode();
        }
    }

    public static JsonFileConfigSource of(Path jsonPath) {
        return new JsonFileConfigSource(jsonPath);
    }

    public void bindOnChange(Path jsonPath) {
        try {
            this.fileWatcher = new FileWatcher(jsonPath).listener(this::onJsonFileChange).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onJsonFileChange(FileWatcher.ChangeEvent changeEvent) {
        var oldConfigMapping = this.configMapping;
        this.configMapping = loadFromJsonFile(jsonPath);
        callOnChange(oldConfigMapping, this.configMapping);
    }

    /// 配置文件丢失异常
    private static class JsonConfigFileMissingException extends Exception {

    }

    private static class JsonConfigFileNotObjectException extends Exception {

    }

}
