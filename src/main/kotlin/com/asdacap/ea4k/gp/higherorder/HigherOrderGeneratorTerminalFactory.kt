package com.asdacap.ea4k.gp.higherorder

import com.asdacap.ea4k.gp.BaseTreeNode
import com.asdacap.ea4k.gp.CallCtxFunction
import com.asdacap.ea4k.gp.NodeType
import com.asdacap.ea4k.gp.TreeNodeFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

/**
 * Generator can't use the FromFuncTreeNodeFactory as it need to store the generated value during serialization
 */
class HigherOrderGeneratorTerminalFactory<R: Any>(
    val generator: () -> R,
    override val returnType: NodeType,
) : TreeNodeFactory<CallCtxFunction<R>> {

    override fun createNode(children: List<BaseTreeNode<*>>): BaseTreeNode<CallCtxFunction<R>> {
        return TreeNode(generator(), generator, returnType)
    }

    override fun canSerialize(tree: BaseTreeNode<*>): Boolean {
        return tree is TreeNode<*> && tree.generator == generator
    }

    override fun serialize(tree: BaseTreeNode<*>, objectMapper: ObjectMapper): JsonNode {
        val eTree = tree as TreeNode<R>
        return objectMapper.valueToTree(eTree.constant)
    }

    override fun deserialize(nodeInfo: JsonNode, children: List<BaseTreeNode<*>>): BaseTreeNode<CallCtxFunction<R>> {
        val asConstant = ObjectMapper().treeToValue(nodeInfo, generator.reflect()!!.returnType.jvmErasure.java) as R
        return TreeNode(asConstant, generator, returnType)
    }

    class TreeNode<R>(val constant: R, val generator: () -> R, override val returnType: NodeType): BaseTreeNode<CallCtxFunction<R>>() {
        override fun evaluate(): CallCtxFunction<R> {
            return CallCtxFunction { constant }
        }

        override fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
            return otherTree is TreeNode<*> && otherTree.constant == constant
        }

        override fun replaceChildren(newChildren: List<BaseTreeNode<*>>): BaseTreeNode<CallCtxFunction<R>> {
            return TreeNode(constant, generator, returnType)
        }
    }

    override val args: List<NodeType> = listOf()
}
