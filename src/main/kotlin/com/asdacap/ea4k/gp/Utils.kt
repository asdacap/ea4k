package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.full.createType

object Utils {
    /**
     *Create a tree node that always return a constant.
     *The resulting tree node is not serializable
     */
    fun <R> createConstantTreeNode(constant: R, type: NodeType = KotlinNodeType(constant!!::class.createType())): TreeNode<R> {
        return FromFuncTreeNode.Factory({ constant }, type).createNode(listOf())
    }

    /**
     * Static global object mapper
     */
    val objectMapper = ObjectMapper()
}

