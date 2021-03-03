package com.asdacap.ea4k.gp.functional

import com.asdacap.ea4k.gp.KotlinNodeType
import com.asdacap.ea4k.gp.NodeType
import kotlin.reflect.KType

data class FunctionNodeType(val innerType: NodeType): NodeType {
    override fun isAssignableTo(otherNodeType: NodeType): Boolean {
        return otherNodeType is FunctionNodeType && innerType.isAssignableTo(otherNodeType.innerType)
    }

    companion object {
        fun functionalNodeTypeFromKType(ktype: KType): FunctionNodeType {
            return FunctionNodeType(KotlinNodeType(ktype))
        }
    }
}