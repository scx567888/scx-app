package dev.scx.config;

import dev.scx.node.ObjectNode;

import java.util.function.BiConsumer;

/// AbstractConfigSource
///
/// @author scx567888
/// @version 0.0.1
public abstract class AbstractConfigSource implements ScxConfigSource {

    protected ObjectNode value;
    private BiConsumer<ObjectNode, ObjectNode> changeListener;

    @Override
    public void onChange(BiConsumer<ObjectNode, ObjectNode> changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    public ObjectNode value() {
        return value;
    }

    protected void callOnChange(ObjectNode oldValue, ObjectNode newValue) {
        if (changeListener != null) {
            this.changeListener.accept(oldValue, newValue);
        }
    }

}
