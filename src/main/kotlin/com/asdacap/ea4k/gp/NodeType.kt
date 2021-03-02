package com.asdacap.ea4k.gp

import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf

interface NodeType {
    fun isAssignableTo(otherNodeType: NodeType): Boolean
}

data class KotlinNodeType(val kType: KType): NodeType {
    override fun isAssignableTo(otherNodeType: NodeType): Boolean {
        if (otherNodeType is KotlinNodeType) {
            return otherNodeType.kType.isSupertypeOf(kType)
        }
        return false
    }
}
