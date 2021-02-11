package ea4k.gp

import kotlin.reflect.KType

/**
 * The tree node itself.
 * This is what gets cloned and called.
 */
abstract class BaseTreeNode<out R> {
    /**
     * An array of the treeNode's children.
     * The items may get replaced at any time, so subclass may want to avoid storing children separately
     * Subclass may want to override this and provide a custom list for special handling
     */
    open val children: MutableList<BaseTreeNode<*>> = mutableListOf()

    /**
     * Return true of this tree node type will always return the same result if its children always
     * return the same result
     */
    open val isPure: Boolean = true

    /**
     * Kinda same as isPure, but for all its subtree.
     */
    val isSubtreeConstant: Boolean
        get() {
            return isPure && children.all { it.isSubtreeConstant }
        }

    /**
     * Get the returnType of this tree node
     */
    abstract val returnType: KType

    /**
     * Actually call the treeNode to evaluate its result. A CallCtx is given to its. The CallCtx
     * contains the arguments for the whole tree. It is treated as the environment, and therefore
     * the tree node that depends on it must not be pure.
     */
    abstract fun call(ctx: CallCtx): R

    /**
     * Clone this treeNode. This method is also responsible for cloning its's children.
     */
    abstract fun clone(): BaseTreeNode<R>

    /**
     * Create a new tree that is optimized for evaluation. The resulting tree may not need be
     * serializable, but is should behave the same as when its not optimized
     */
    open fun optimizeForEvaluation(): BaseTreeNode<R> = this

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
     * Return a list of pair of tree node and an integer.
     * The tree node is basically a parent, and the integer is an index of a child.
     * This makes it possible to replace a child of a node.
     * A downside of that is that, the root node can never be swapped.
     * It also detect cycles
     */
    fun iterateAllWithParentAndIndex(): List<Pair<BaseTreeNode<*>, Int>> {
        return iterateDfs(mutableSetOf())
    }

    private fun iterateDfs(dedup: MutableSet<BaseTreeNode<*>>): List<Pair<BaseTreeNode<*>, Int>> {
        if (dedup.contains(this)) {
            throw Exception("Cycle detected!")
        }
        dedup.add(this)
        val result = children.flatMap { it.iterateDfs(dedup) } +
                children.mapIndexed { i, tree ->
                    Pair(this, i)
                }
        dedup.remove(this)
        return result
    }

    /**
     * Return the size of this subTree
     */
    val size: Int
        get() {
            val nodeSet: MutableSet<BaseTreeNode<*>> = mutableSetOf()
            return getSizeDfs(nodeSet, this)
        }

    fun getSizeDfs(nodeSet: MutableSet<BaseTreeNode<*>>, treeNode: BaseTreeNode<*>): Int {
        if (nodeSet.contains(treeNode)) {
            return 0
        }
        nodeSet.add(treeNode)
        return treeNode.children.map { getSizeDfs(nodeSet, it) }.sum() + 1
    }

    override fun equals(other: Any?): Boolean {
        if (other is BaseTreeNode<*>) {
            return isSubtreeEffectivelySame(other)
        }
        return super.equals(other)
    }
}
