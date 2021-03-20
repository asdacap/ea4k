package com.asdacap.ea4k.gp

import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.typeOf

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

        inline fun <reified T> fromKotlinNodeType(): KotlinNodeType {
            return KotlinNodeType(typeOf<T>())
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
