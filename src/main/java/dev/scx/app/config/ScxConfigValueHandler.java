package dev.scx.app.config;

import dev.scx.node.Node;

/// ScxConfigValueHandler
///
/// @author scx567888
/// @version 0.0.1
@FunctionalInterface
public interface ScxConfigValueHandler<T> {

    T handle(String keyPath, Node rawValue);

}
