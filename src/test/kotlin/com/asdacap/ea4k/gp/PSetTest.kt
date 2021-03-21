package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.FromFuncTreeNode.Companion.fromConstant
import com.asdacap.ea4k.gp.FromFuncTreeNode.Companion.fromFunction
import com.asdacap.ea4k.gp.Generator.safeGenerate
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.reflect.typeOf

class PSetTest {

    val pset: PSet<Float> = PSet(NodeType.fromKotlinNodeType(typeOf<Float>()))

    init {
        pset.addTreeNodeFactory("constant0", fromConstant(0f))
        pset.addTreeNodeFactory("constant1", fromConstant(1f))
        pset.addTreeNodeFactory("primitive", fromFunction(::primitive))
    }

    fun primitive(f1: Float, f2: Float): Float {
        return f1 + f2
    }

    @Test
    fun testSerializeDeserialize() {
        val node = safeGenerate(pset, 0, 2, { i, i2 -> i == i2 }, NodeType.fromKotlinNodeType<Float>())
        val serialized = pset.serialize(node)
        val deserialized = pset.deserialize(serialized)
        assertTrue(node.isSubtreeEffectivelySame(deserialized))
    }

    @Test
    fun testWhenRequestedImpossibleTypePrimitive_thenReturnNull() {
        val factory = pset.selectPrimitiveAssignableTo(NodeType.fromKotlinNodeType<Boolean>())
        assertNull(factory)
    }

    @Test
    fun testWhenRequestedImpossibleTypeTerminal_thenReturnNull() {
        val factory = pset.selectTerminalAssignableTo(NodeType.fromKotlinNodeType<Boolean>())
        assertNull(factory)
    }
}