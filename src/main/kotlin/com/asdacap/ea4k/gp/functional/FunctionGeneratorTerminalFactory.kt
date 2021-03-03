package com.asdacap.ea4k.gp.functional

import com.asdacap.ea4k.gp.BaseTreeNode
import com.asdacap.ea4k.gp.Function
import com.asdacap.ea4k.gp.NodeType
import com.asdacap.ea4k.gp.TreeNodeFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

/**
 * Generator can't use the FromFuncTreeNodeFactory as it need to store the generated value during serialization
 */
class FunctionGeneratorTerminalFactory<R: Any>(
    val generator: () -> R,
    override val returnType: NodeType,
) : TreeNodeFactory<Function<R>> {

    override fun createNode(children: List<BaseTreeNode<*>>): BaseTreeNode<Function<R>> {
        return TreeNode(generator(), generator, returnType, this)
    }

    override fun serialize(tree: BaseTreeNode<*>, objectMapper: ObjectMapper): JsonNode {
        val eTree = tree as TreeNode<R>
        return objectMapper.valueToTree(eTree.constant)
    }

    override fun deserialize(nodeInfo: JsonNode, children: List<BaseTreeNode<*>>): BaseTreeNode<Function<R>> {
        val asConstant = ObjectMapper().treeToValue(nodeInfo, generator.reflect()!!.returnType.jvmErasure.java) as R
        return TreeNode(asConstant, generator, returnType, this)
    }

    class TreeNode<R>(
        val constant: R,
        val generator: () -> R,
        override val returnType: NodeType,
        override val treeNodeFactory: TreeNodeFactory<Function<R>>
    ): BaseTreeNode<Function<R>>() {
        override fun evaluate(): Function<R> {
            return Function { constant }
        }

        override fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
            return otherTree is TreeNode<*> && otherTree.constant == constant
        }

        override fun replaceChildren(newChildren: List<BaseTreeNode<*>>): BaseTreeNode<Function<R>> {
            return TreeNode(constant, generator, returnType, treeNodeFactory)
        }
    }

    override val args: List<NodeType> = listOf()
}
