package com.asdacap.ea4k.gp

import kotlin.random.Random
import kotlin.reflect.full.createType

object Utils {
    /**
     *Create a tree node that always return a constant.
     *The resulting tree node is not serializable
     */
    fun <R> createConstantTreeNode(constant: R): BaseTreeNode<R> {
        return FromFuncTreeNodeFactory.TreeNode({ ctx, children ->
            constant
        }, constant!!::class.createType(), listOf())
    }
}

