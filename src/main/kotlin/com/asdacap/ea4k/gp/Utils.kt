package com.asdacap.ea4k.gp

import kotlin.reflect.full.createType

object Utils {
    /**
     *Create a tree node that always return a constant.
     *The resulting tree node is not serializable
     */
    fun <R> createConstantTreeNode(constant: R, type: NodeType = KotlinNodeType(constant!!::class.createType())): BaseTreeNode<R> {
        return FromFuncTreeNodeFactory({ constant }, type).createNode(listOf())
    }
}

