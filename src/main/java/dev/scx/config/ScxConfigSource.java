package dev.scx.config;

import dev.scx.node.ObjectNode;

import java.util.function.BiConsumer;

/// 配置源
///
/// @author scx567888
/// @version 0.0.1
public interface ScxConfigSource {

    /// 获取当前配置值
    ///
    /// 注意配置值必须被归一化为 树形结构.
    /// 换句话说 所有 "." 分隔符都应看作 树形结构的一层.
    ///
    /// @return 当前配置值
    ObjectNode value();

    /// 监听配置值变化
    ///
    /// 如果当前配置源支持监听配置变化 (如: 文件).
    ///
    default void onChange(BiConsumer<ObjectNode, ObjectNode> changeListener) {

    }

}
