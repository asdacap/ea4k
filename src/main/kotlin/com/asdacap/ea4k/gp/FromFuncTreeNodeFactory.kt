package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KCallable
import kotlin.reflect.jvm.reflect
import kotlin.reflect.typeOf

/**
 * Generate a tree node that essentially call the given function.
 *
 * Turns out, a lot of other type of tree node can be made with this factory.
 * So most tree node type basically translate back to this tree node
 */
class FromFuncTreeNodeFactory <R>(
    // The given function accept the call context and an array of child tree node.
    // The implementation may skip calling she child tree node for optimization purpose.
    val func: (Array<Any>) -> R,

    // The desired type of child for this tree node type
    override val args: List<NodeType>,

    // The return type of this tree node type.
    // If not defined it may try to use reflection to detect it, but it does not work when passing lambda.
    override val returnType: NodeType = KotlinNodeType(func.reflect()!!.returnType),
): TreeNodeFactory<R> {

    companion object {

        /**
         * Create a tree node factory from a kotlin KCallable. This will use reflection to detect the callable's
         * argument and return type.
         */
        inline fun <reified R> fromFunction(func: KCallable<R>): FromFuncTreeNodeFactory<R> {
            return FromFuncTreeNodeFactory({ input ->
                func.call(*input)!!
            }, func.parameters.map { KotlinNodeType(it.type) }, KotlinNodeType(typeOf<R>()))
        }

        /**
         * Create a tree node factory that always return the same result.
         */
        inline fun <reified R> fromConstant(constant: R): FromFuncTreeNodeFactory<R> {
            return FromFuncTreeNodeFactory({ input ->
                constant
            }, listOf(), KotlinNodeType(typeOf<R>()))
        }
    }

    override fun createNode(children: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        return TreeNode(func, returnType, children.toList())
    }

    override fun canSerialize(tree: BaseTreeNode<*>): Boolean {
        return tree is TreeNode && tree.func == this.func
    }

    override fun serialize(tree: BaseTreeNode<*>, objectMapper: ObjectMapper): JsonNode {
        return objectMapper.createObjectNode()
    }

    override fun deserialize(nodeInfo: JsonNode, children: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        return createNode(children.toList())
    }

    class TreeNode <R>(
        val func: (Array<Any>) -> R,
        override val returnType: NodeType,
        override val children: List<BaseTreeNode<*>>
    ) : BaseTreeNode<R>() {
        val childrenArray = children.toTypedArray()

        override fun call(): R {
            // Hot code!
            val size = childrenArray.size
            val newArray = arrayOfNulls<Any>(size)
            var i = 0
            while(i < size) {
                newArray[i] = childrenArray[i].call()
                i++
            }
            return func.invoke(newArray as Array<Any>)
        }

        override fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
            return otherTree is TreeNode
                    && otherTree.func == func
        }

        override fun replaceChildren(newChildren: List<BaseTreeNode<*>>): BaseTreeNode<R> {
            return TreeNode(func, returnType, newChildren)
        }
    }
}
