package dev.scx.app.config;

import java.util.HashMap;
import java.util.Map;

/// ScxFeatureConfig
///
/// @author scx567888
/// @version 0.0.1
public final class ScxFeatureConfig {

    /// Scx 特性列表
    private final Map<ScxFeature<?>, Object> map = new HashMap<>();

    public <T> ScxFeatureConfig set(ScxFeature<T> scxFeature, T state) {
        map.put(scxFeature, state);
        return this;
    }

    public <T> ScxFeatureConfig remove(ScxFeature<T> scxFeature) {
        map.remove(scxFeature);
        return this;
    }

    /// 获取特性的值 如果没有显式设置 则返回默认值
    ///
    /// @param scxFeature s
    /// @param <T>        a T class
    /// @return s
    @SuppressWarnings("unchecked")
    public <T> T get(ScxFeature<T> scxFeature) {
        return (T) map.getOrDefault(scxFeature, scxFeature.defaultValue());
    }

}
