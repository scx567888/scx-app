package dev.scx.app._old.config.source;

import dev.scx.app._old.config.NodeHelper;
import dev.scx.config.AbstractConfigSource;
import dev.scx.config.ScxConfigSource;
import dev.scx.node.ObjectNode;

/// MultiConfigSource
///
/// @author scx567888
/// @version 0.0.1
public class MultiConfigSource extends AbstractConfigSource {

    private final ScxConfigSource[] sources;

    public MultiConfigSource(ScxConfigSource... sources) {
        this.sources = sources;
        this.value = loadFromSources(sources);
        bindOnChange(sources);
    }

    public static ObjectNode loadFromSources(ScxConfigSource... sources) {
        var configMapping = new ObjectNode();
        for (var scxConfigSource : sources) {
//            NodeHelper.merge(configMapping, scxConfigSource.configMapping());
        }
        return configMapping;
    }

    public void bindOnChange(ScxConfigSource... sources) {
        for (var source : sources) {
            source.onChange(this::callOnChange0);
        }
    }

    private void callOnChange0(ObjectNode oldValue, ObjectNode newValue) {
//        var oldConfigMapping = this.configMapping;
//        this.configMapping = loadFromSources(sources);
//        callOnChange(oldConfigMapping, this.configMapping);
    }

}
