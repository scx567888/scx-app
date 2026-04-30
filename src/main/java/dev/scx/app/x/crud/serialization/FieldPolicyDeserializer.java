package dev.scx.app.x.crud.serialization;

import dev.scx.data.field_policy.*;
import dev.scx.node.ArrayNode;
import dev.scx.node.Node;
import dev.scx.node.ObjectNode;
import dev.scx.node.ValueNode;
import dev.scx.reflect.TypeReference;
import dev.scx.serialize.ScxSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.scx.node.NullNode.NULL;
import static dev.scx.serialize.ScxSerialize.convertObject;

public class FieldPolicyDeserializer {

    public static FieldPolicy deserializeFieldPolicyFromJson(String json) throws DeserializationException {
        try {
            var node = ScxSerialize.fromJson(json);
            return deserializeFieldPolicy(node);
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
    }

    public static FieldPolicy deserializeFieldPolicy(Node v) throws DeserializationException {
        if (v == null || v == NULL || !(v instanceof ObjectNode objNode)) {
            throw new DeserializationException("FieldPolicy node is null or not an object");
        }

        var typeNode = objNode.get("@type");
        if (!(typeNode instanceof ValueNode vn) || !"FieldPolicy".equals(vn.asString())) {
            throw new DeserializationException("Unknown or missing @type for FieldPolicy");
        }

        var filterModeNode = objNode.get("filterMode");
        if (!(filterModeNode instanceof Node)) {
            throw new DeserializationException("filterMode is missing or null");
        }
        var filterMode = convertObject(filterModeNode, FilterMode.class);

        var policy = new FieldPolicyImpl(filterMode);

        var fieldNamesNode = objNode.get("fieldNames");
        if (!(fieldNamesNode instanceof ArrayNode arrayNode)) {
            throw new DeserializationException("fieldNames is null or not an array");
        }

        List<String> fieldNames = new ArrayList<>();
        for (Node fn : arrayNode) {
            if (!(fn instanceof ValueNode vNode)) {
                throw new DeserializationException("Each fieldName must be a ValueNode");
            }
            fieldNames.add(vNode.asString());
        }

        if (filterMode == FilterMode.INCLUDED) {
            policy.include(fieldNames.toArray(String[]::new));
        } else if (filterMode == FilterMode.EXCLUDED) {
            policy.exclude(fieldNames.toArray(String[]::new));
        }

        var virtualFieldsNode = objNode.get("virtualFields");
        if (!(virtualFieldsNode instanceof ArrayNode vfArray)) {
            throw new DeserializationException("virtualFields is null or not an array");
        }

        var virtualFields = new ArrayList<VirtualField>();
        for (Node vfNode : vfArray) {
            virtualFields.add(deserializeVirtualField(vfNode));
        }
        policy.virtualFields(virtualFields.toArray(VirtualField[]::new));

        var assignFieldsNode = objNode.get("assignFields");
        if (!(assignFieldsNode instanceof ArrayNode afArray)) {
            throw new DeserializationException("assignFields is null or not an array");
        }

        var assignFields = new ArrayList<AssignField>();
        for (Node afNode : afArray) {
            assignFields.add(deserializeAssignField(afNode));
        }
        policy.assignFields(assignFields.toArray(AssignField[]::new));

        var ignoreNullNode = objNode.get("ignoreNull");
        if (!(ignoreNullNode instanceof ValueNode ignoreVN)) {
            throw new DeserializationException("ignoreNull is missing or invalid");
        }
        policy.ignoreNull(ignoreVN.asBoolean());

        var ignoreNullsNode = objNode.get("ignoreNulls");
        if (!(ignoreNullsNode instanceof ObjectNode ignoreMapNode)) {
            throw new DeserializationException("ignoreNulls is not an object");
        }

        Map<String, Boolean> ignoreNulls = convertObject(ignoreMapNode, new TypeReference<>() {});
        for (var e : ignoreNulls.entrySet()) {
            policy.ignoreNull(e.getKey(), e.getValue());
        }

        return policy;
    }

    private static VirtualField deserializeVirtualField(Node node) throws DeserializationException {
        if (node == null || node== NULL || !(node instanceof ObjectNode objNode)) {
            throw new DeserializationException("VirtualField must be an ObjectNode");
        }

        var typeNode = objNode.get("@type");
        if (!(typeNode instanceof ValueNode vn) || !"VirtualField".equals(vn.asString())) {
            throw new DeserializationException("Invalid or missing @type for VirtualField");
        }

        var nameNode = objNode.get("virtualFieldName");
        var exprNode = objNode.get("expression");

        if (!(nameNode instanceof ValueNode nameVN) || !(exprNode instanceof ValueNode exprVN)) {
            throw new DeserializationException("virtualFieldName or expression is invalid in VirtualField");
        }

        return new VirtualField(nameVN.asString(), exprVN.asString());
    }

    private static AssignField deserializeAssignField(Node node) throws DeserializationException {
        if (node == null || node==NULL || !(node instanceof ObjectNode objNode)) {
            throw new DeserializationException("AssignField must be an ObjectNode");
        }

        var typeNode = objNode.get("@type");
        if (!(typeNode instanceof ValueNode vn) || !"AssignField".equals(vn.asString())) {
            throw new DeserializationException("Invalid or missing @type for AssignField");
        }

        var fieldNameNode = objNode.get("fieldName");
        var exprNode = objNode.get("expression");

        if (!(fieldNameNode instanceof ValueNode fnVN) || !(exprNode instanceof ValueNode exprVN)) {
            throw new DeserializationException("fieldName or expression is invalid in AssignField");
        }

        return new AssignField(fnVN.asString(), exprVN.asString());
    }

}
