package com.asdacap.ea4k.gp

/**
 * The tree node itself.
 * A tree node also represent a tree which can be evaluated to return a result of type R.
 * A tree node should be immmutable.
 */
abstract class BaseTreeNode<out R> {
    /**
     * A list of the treeNode's children.
     */
    open val children: List<BaseTreeNode<*>> = listOf()

    /**
     * Get the returnType of this tree node
     */
    abstract val returnType: NodeType

    /**
     * Evaluate the result of this tree node. Tree node generally will take into account its child to get the result.
     */
    abstract fun evaluate(): R

    /**
     * Create a copy of this treeNode with its children replaced
     */
    abstract fun replaceChildren(newChildren: List<BaseTreeNode<*>>): BaseTreeNode<out R>

    /**
     * Create a copy of this treeNode with its children on the particular index swapped
     */
    open fun replaceChild(index: Int, replaceWith: BaseTreeNode<*>): BaseTreeNode<out R> {
        return replaceChildren(children.mapIndexed { cIndex, baseTreeNode ->
            if (cIndex == index) {
                replaceWith
            } else {
                baseTreeNode
            }
        })
    }

    /**
     * Return true if this node is effectively the same as the other node.
     * Does not include its children
     */
    abstract fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean

    /**
     * Return true if this subtree is effectively the same as the otherTree
     */
    open fun isSubtreeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
        return isNodeEffectivelySame(otherTree)
                && this.children.size == otherTree.children.size
                && this.children.zip(otherTree.children).all { it.first.isSubtreeEffectivelySame(it.second) }
    }

    /**
     * Return a list of tree node for the whole subtree
     * It also detect cycles
     * Iteration is in postOrder order
     */
    fun iterateAll(): List<BaseTreeNode<*>> {
        return iterateDfs(mutableSetOf())
    }

    private fun iterateDfs(dedup: MutableSet<BaseTreeNode<*>>): List<BaseTreeNode<*>> {
        if (dedup.contains(this)) {
            throw Exception("Cycle detected!")
        }
        dedup.add(this)
        val result = children.flatMap { it.iterateDfs(dedup) } + this
        dedup.remove(this)
        return result
    }

    /**
     * Return the size of this subTree
     */
    val size: Int by lazy { children.map { it.size }.sum() + 1 }
}

fun BaseTreeNode<*>.replaceChild(toReplace: BaseTreeNode<*>, replaceWith: BaseTreeNode<*>): BaseTreeNode<*> {
    if (this === toReplace) {
        return replaceWith
    }

    // This does mean that only one replacement is done
    var replaced = false
    val newChilds = children.map {
        if (replaced) {
            it
        } else {
            val newChild = it.replaceChild(toReplace, replaceWith)
            if (newChild !== it) {
                replaced = true
            }
            newChild
        }
    }
    if (replaced) {
        return replaceChildren(newChilds)
    }
    return this
}