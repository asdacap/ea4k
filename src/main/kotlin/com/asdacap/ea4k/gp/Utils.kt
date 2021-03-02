package com.asdacap.ea4k.gp

import kotlin.reflect.KType
import kotlin.reflect.full.createType

object Utils {
    /**
     *Create a tree node that always return a constant.
     *The resulting tree node is not serializable
     */
    fun <R> createConstantTreeNode(constant: R, type: KType = constant!!::class.createType()): BaseTreeNode<R> {
        return FromFuncTreeNodeFactory.TreeNode({ input ->
            constant
        }, KotlinNodeType(type), listOf())
    }
}

