package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.KCallable
import kotlin.reflect.jvm.reflect
import kotlin.reflect.typeOf

/**
 * A tree node that execute the given function, passing the results from its children to the function as array
 *
 * Turns out, a lot of other type of tree node can be made with this factory.
 * So most tree node type basically translate back to this tree node type.
 */
class FromFuncTreeNode <R>(
    val func: (Array<Any>) -> R,
    override val children: List<TreeNode<*>>,
    override val factory: TreeNodeFactory<R>,
) : TreeNode<R>() {
    companion object {
        /**
         * Create a tree node factory from a kotlin KCallable. This will use reflection to detect the callable's
         * argument and return type.
         */
        inline fun <reified R> fromFunction(
            func: KCallable<R>,
            type: NodeType = KotlinNodeType(typeOf<R>()),
            parameters: List<NodeType> = func.parameters.map { KotlinNodeType(it.type) }
        ): Factory<R> {
            return Factory({ input ->
                func.call(*input)!!
            }, type, parameters)
        }

        /**
         * Create a tree node factory that always return the same result.
         */
        inline fun <reified R> fromConstant(
            constant: R,
            type: NodeType = KotlinNodeType(typeOf<R>())
        ): Factory<R> {
            return Factory({
                constant
            }, type, listOf())
        }
    }

    // Array for optimization reason
    private val childrenArray = children.toTypedArray()

    override fun evaluate(): R {
        // Hot code!
        val size = childrenArray.size
        val newArray = arrayOfNulls<Any>(size)
        var i = 0
        while(i < size) {
            newArray[i] = childrenArray[i].evaluate()
            i++
        }
        @Suppress("UNCHECKED_CAST")
        return func.invoke(newArray as Array<Any>)
    }

    override fun replaceChildren(newChildren: List<TreeNode<*>>): TreeNode<R> {
        return FromFuncTreeNode(func, newChildren, factory)
    }

    class Factory <R>(
        // The given function accept value returned by the children evaluate function
        val func: (Array<Any>) -> R,

        // The return type of this tree node type.
        // If not defined it may try to use reflection to detect it, but it does not work when passing lambda.
        override val returnType: NodeType = KotlinNodeType(func.reflect()!!.returnType),

        // The desired type of children for this tree node type
        override val args: List<NodeType> = listOf(),
    ): TreeNodeFactory<R> {

        override fun createNode(children: List<TreeNode<*>>, state: JsonNode?): TreeNode<R> {
            return FromFuncTreeNode(func, children.toList(), this)
        }
    }
}
