package dev.scx.app._old.config;

/// ScxFeature
///
/// @author scx567888
/// @version 0.0.1
public interface ScxFeature<T> {

    /// 默认值
    ///
    /// @return 默认值
    T defaultValue();

    /// a
    ///
    /// @return a
    default String name() {
        return this.getClass().getName();
    }

}
