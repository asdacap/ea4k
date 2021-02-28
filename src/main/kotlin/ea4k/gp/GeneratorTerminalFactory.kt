package ea4k.gp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

/**
 * Generator can't use the FromFuncTreeNodeFactory as it need to store the generated value during serialization
 */
class GeneratorTerminalFactory<R: Any>(val generator: () -> R, override val returnType: KType = generator.reflect()!!.returnType): TreeNodeFactory<R> {
    override fun createNode(children: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        return TreeNode(generator(), generator)
    }

    override fun canSerialize(tree: BaseTreeNode<*>): Boolean {
        return tree is TreeNode && tree.generator == generator
    }

    override fun serialize(tree: BaseTreeNode<*>, objectMapper: ObjectMapper): JsonNode {
        val eTree = tree as TreeNode<R>
        return objectMapper.valueToTree(eTree.constant)
    }

    override fun deserialize(nodeInfo: JsonNode, children: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        val asConstant = ObjectMapper().treeToValue(nodeInfo, returnType.jvmErasure.java) as R
        return TreeNode(asConstant, generator)
    }

    class TreeNode<R>(val constant: R, val generator: () -> R): BaseTreeNode<R>() {
        override val returnType: KType = constant!!::class.createType()

        override fun clone(): BaseTreeNode<R> {
            return TreeNode(constant, generator)
        }

        override fun call(ctx: CallCtx): R {
            return constant
        }

        override fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
            return otherTree is TreeNode && otherTree.constant == constant
        }

        override fun replaceChildren(newChildren: List<BaseTreeNode<*>>): BaseTreeNode<R> {
            return TreeNode(constant, generator)
        }
    }

    override val args: List<KType> = listOf()
}
