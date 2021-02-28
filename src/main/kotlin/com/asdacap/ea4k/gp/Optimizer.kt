package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.Utils.createConstantTreeNode

/**
 * A tree optimizer is a function that transform a tree node into another
 * tree node, possibly optimized or with less overall tree node.
 */
typealias TreeOptimizer<R> = (BaseTreeNode<R>) -> BaseTreeNode<R>

object Optimizer {
    /**
     * Optimize tree for execution
     * This will first, recursively call optimizeTree on its children,
     * Then it will attempt to transform the tree to a constant
     * Then it will call the treeNode's optimizeForEvaluation
     */
    fun <R> optimizeForEvaluation(root: BaseTreeNode<R>): BaseTreeNode<R> {
        return doOptimizeForEvaluation(root)
    }

    private fun <R> doOptimizeForEvaluation(root: BaseTreeNode<R>): BaseTreeNode<R> {
        val root = root.replaceChildren(root.children.map { treeNode ->
            doOptimizeForEvaluation(treeNode)
        })
        val constantNode = optimizeConstants(root)
        if (constantNode!=null) {
            return constantNode
        }
        return root.optimizeForEvaluation()
    }

    /**
     * A tree can be autoshake if its subtree is pure.
     * It just evaluate itself, and got replaced by a constant tree node.
     */
    fun <R> optimizeConstants(tree: BaseTreeNode<R>): BaseTreeNode<R>? {
        if (!tree.isSubtreeConstant || tree.children.isEmpty()){
            return null
        }

        val value = tree.call(CallCtx(arrayOf()))
        return createConstantTreeNode(value)
    }
}
