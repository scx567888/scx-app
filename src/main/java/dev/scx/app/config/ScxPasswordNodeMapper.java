package dev.scx.app.config;

import dev.scx.node.StringNode;
import dev.scx.object.NodeToObjectException;
import dev.scx.object.ObjectToNodeException;
import dev.scx.object.x.context.NodeToObjectContext;
import dev.scx.object.x.context.ObjectToNodeContext;
import dev.scx.object.x.mapper.TypeNodeMapper;
import dev.scx.reflect.TypeInfo;

import static dev.scx.reflect.ScxReflect.typeOf;

public class ScxPasswordNodeMapper implements TypeNodeMapper<ScxPassword, StringNode> {

    @Override
    public TypeInfo valueType() {
        return typeOf(ScxPassword.class);
    }

    @Override
    public Class<StringNode> nodeType() {
        return StringNode.class;
    }

    @Override
    public StringNode valueToNode(ScxPassword value, ObjectToNodeContext context) throws ObjectToNodeException {
        return null;
    }

    @Override
    public ScxPassword nodeToValue(StringNode node, NodeToObjectContext context) throws NodeToObjectException {
        return null;
    }
}
