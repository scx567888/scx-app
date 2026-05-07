package dev.scx.app.config;

import dev.scx.node.Node;
import dev.scx.reflect.TypeReference;

import java.io.File;

/// 配置文件类
///
/// @author scx567888
/// @version 0.0.1
public interface ScxConfig {

    static ScxConfig of(File jsonFile,ScxEnvironment scxEnvironment) {
        return new ScxConfigImpl(jsonFile,scxEnvironment);
    }

    Node get(String path);

    <T> T get(String path, Class<T> type);

    <T> T get(String path, TypeReference<T> type);

    <T> T getOrDefault(String path, Class<T> type, T defaultValue);

    <T> T getOrDefault(String path, TypeReference<T> type, T defaultValue);

}
