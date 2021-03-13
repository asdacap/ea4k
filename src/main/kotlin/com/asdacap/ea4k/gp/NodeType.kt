package com.asdacap.ea4k.gp

import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf

/**
 * A NodeType is a type that represent a type of a base tree node. Specifically, it is used by the generator to
 * assign child tree node.
 */
interface NodeType {
    fun isAssignableTo(otherNodeType: NodeType): Boolean

    companion object {
        fun fromKotlinNodeType(kType: KType): KotlinNodeType {
            return KotlinNodeType(kType)
        }
    }
}

/**
 * A NodeType that encapsulate a kotlin/JVM type.
 */
data class KotlinNodeType(val kType: KType): NodeType {
    override fun isAssignableTo(otherNodeType: NodeType): Boolean {
        if (otherNodeType is KotlinNodeType) {
            return otherNodeType.kType.isSupertypeOf(kType)
        }
        return false
    }
}
