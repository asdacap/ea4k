package com.asdacap.ea4k.gp

import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf

/**
 * A NodeType is a type that represent what can be assigned as a child of a tree node.
 */
interface NodeType {
    fun isAssignableTo(otherNodeType: NodeType): Boolean
}

/**
 * The most obvious NodeType is a type that encapsulate kotlin/JVM type.
 */
data class KotlinNodeType(val kType: KType): NodeType {
    override fun isAssignableTo(otherNodeType: NodeType): Boolean {
        if (otherNodeType is KotlinNodeType) {
            return otherNodeType.kType.isSupertypeOf(kType)
        }
        return false
    }
}
