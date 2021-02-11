package ea4k.gp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KType

class RootTreeNodeFactory<R>(
    override val returnType: KType
): TreeNodeFactory<R> {
    override val args: List<KType> = listOf(returnType)

    override fun serialize(tree: BaseTreeNode<*>, objectMapper: ObjectMapper): JsonNode {
        return objectMapper.createObjectNode()
    }

    override fun deserialize(nodeInfo: JsonNode, children: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        return RootTreeNode(children[0] as BaseTreeNode<R>)
    }

    override fun createNode(children: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        return RootTreeNode(children[0] as BaseTreeNode<R>)
    }

    override fun canSerialize(tree: BaseTreeNode<*>): Boolean {
        return tree is RootTreeNode
    }
}