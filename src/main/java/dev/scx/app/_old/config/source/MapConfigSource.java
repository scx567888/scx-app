package dev.scx.app._old.config.source;

import dev.scx.app._old.config.NodeHelper;
import dev.scx.config.AbstractConfigSource;
import dev.scx.node.ObjectNode;

import java.util.Map;

/// MapConfigSource
///
/// @author scx567888
/// @version 0.0.1
public final class MapConfigSource extends AbstractConfigSource {

    private MapConfigSource(Map<String, Object> map) {
        this.value = loadFromMap(map);
    }

    public static ObjectNode loadFromMap(Map<String, Object> map) {
        var configMapping = new ObjectNode();
        map.forEach((k, v) -> NodeHelper.set(configMapping, k, v));
        return configMapping;
    }

    public static MapConfigSource of(Map<String, Object> configMapping) {
        return new MapConfigSource(configMapping);
    }

}
