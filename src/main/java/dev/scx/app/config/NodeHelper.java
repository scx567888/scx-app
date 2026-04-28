package dev.scx.app.config;

import dev.scx.node.ArrayNode;
import dev.scx.node.Node;
import dev.scx.node.ObjectNode;
import dev.scx.object.NodeToObjectException;
import dev.scx.object.ObjectToNodeException;
import dev.scx.serialize.ScxSerialize;

//todo 这个类 待重新优化

/// 将 source 合并到 target 中(深合并).
///
/// 合并规则如下：
///
///   - 当 target 与 source 都是 ObjectNode 时, 递归地逐个 key 合并.
///   - 当 target 与 source 都是 ArrayNode 时, source 节点会被追加到 target 的末尾.
///   - 当类型不一致, 或者某个 key 下 target 是值节点, source 也是值节点时, 直接用 source 节点覆盖 target 节点.
///   - 如果 target 和 source 类型不同(且不是 ObjectNode 或 ArrayNode), 则抛出异常.
public class NodeHelper {

    public static void merge(Node target, Node source) {
        if (target instanceof ObjectNode targetObj && source instanceof ObjectNode sourceObj) {
            mergeObjectNode(targetObj, sourceObj);
        } else if (target instanceof ArrayNode targetArr && source instanceof ArrayNode sourceArr) {
            mergeArrayNode(targetArr, sourceArr);
        } else {
            throw new IllegalArgumentException("Cannot merge nodes of type: " + target.getClass() + " and " + source.getClass());
        }
    }

    private static void mergeObjectNode(ObjectNode target, ObjectNode source) {
        for (var entry : source) {
            var key = entry.getKey();
            var sourceNode = entry.getValue();
            var targetNode = target.get(key);
            if (targetNode instanceof ObjectNode && sourceNode instanceof ObjectNode) {
                merge(targetNode, sourceNode);
            } else if (targetNode instanceof ArrayNode && sourceNode instanceof ArrayNode) {
                merge(targetNode, sourceNode);
            } else {
                // 不同类型或者值类型, 直接覆盖
                target.put(key, sourceNode);
            }
        }
    }

    private static void mergeArrayNode(ArrayNode target, ArrayNode source) {
        for (var node : source) {
            target.add(node);
        }
    }

    public static Node get(Node jsonNode, String jsonPath) {
        var paths = jsonPath.split("\\.");
        var result = jsonNode;
        for (var path : paths) {
            if (result != null) {
                if (result instanceof ObjectNode i) {
                    result = i.get(path);
                } else {
                    result = null;
                }
            } else {
                break;
            }
        }
        return result;
    }

    public static Node set(ObjectNode objectNode, String jsonPath, Object object) throws ObjectToNodeException, NodeToObjectException {
        var paths = jsonPath.split("\\.");
        var jsonNode = objectNode;
        for (int i = 0; i < paths.length; i = i + 1) {
            var path = paths[i];
            //最后一个
            if (i == paths.length - 1) {
                put(jsonNode, path, object);
            } else {
                var n = jsonNode.get(path);
                jsonNode = switch (n) {
                    case null -> {
                        var entries = new ObjectNode();
                        jsonNode.put(path, entries);
                        yield entries;
                    }
                    case ObjectNode nn -> nn;
                    default -> throw new RuntimeException("路径中已有数据且不为 Object, 无法 set");
                };
            }
        }
        return objectNode;
    }

    private static void put(ObjectNode objectNode, String fieldName, Object object) throws ObjectToNodeException, NodeToObjectException {
        objectNode.put(fieldName, ScxSerialize.convertObject(object, Node.class));
    }

}
