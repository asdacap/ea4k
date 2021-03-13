package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.JsonNode

/**
 * The tree node itself.
 * A tree node also represent a tree which can be evaluated to return a result of type R.
 * A tree node should be immmutable.
 */
abstract class TreeNode<out R> {

    /**
     * A reference to the factory of this tree node
     */
    abstract val factory: TreeNodeFactory<R>

    /**
     * A list of the treeNode's children.
     */
    open val children: List<TreeNode<*>> = listOf()

    /**
     * The state of the node. If the node does not store any state, then return an empty json object node.
     */
    open val state: JsonNode = Utils.objectMapper.createObjectNode()

    /**
     * Evaluate the result of this tree node. Tree node generally will take into account its child to get the result.
     */
    abstract fun evaluate(): R

    /**
     * Create a copy of this treeNode with its children replaced
     */
    abstract fun replaceChildren(newChildren: List<TreeNode<*>>): TreeNode<out R>

    /**
     * Create a copy of this treeNode with its children on the particular index swapped
     */
    open fun replaceChild(index: Int, replaceWith: TreeNode<*>): TreeNode<out R> {
        return replaceChildren(children.mapIndexed { cIndex, baseTreeNode ->
            if (cIndex == index) {
                replaceWith
            } else {
                baseTreeNode
            }
        })
    }

    /**
     * Replace a child in this subtree with the given subtree. The child is selected by reference equality.
     * Returns a copy of this tree node if a child in the subtree is replaced. Return same object as this tree node
     * if that is not the case.
     */
    fun replaceChild(toReplace: TreeNode<*>, replaceWith: TreeNode<*>): TreeNode<*> {
        // No change
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

    /**
     * Return true if this node is effectively the same as the other node.
     * Does not include its children
     */
    open fun isNodeEffectivelySame(otherTree: TreeNode<*>): Boolean {
        return otherTree.factory == this.factory && otherTree.state == this.state
    }

    /**
     * Return true if this subtree is effectively the same as the otherTree
     */
    open fun isSubtreeEffectivelySame(otherTree: TreeNode<*>): Boolean {
        return isNodeEffectivelySame(otherTree)
                && this.children.size == otherTree.children.size
                && this.children.zip(otherTree.children).all { it.first.isSubtreeEffectivelySame(it.second) }
    }

    /**
     * Return a list of tree node for the whole subtree
     * It also detect cycles
     * Iteration is in postOrder order
     */
    fun iterateAll(): List<TreeNode<*>> {
        return iterateDfs(mutableSetOf())
    }

    private fun iterateDfs(dedup: MutableSet<TreeNode<*>>): List<TreeNode<*>> {
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

    /**
     * Return the height of this subTree
     */
    val height: Int by lazy { (children.maxOfOrNull { it.height } ?: 0) + 1 }
}