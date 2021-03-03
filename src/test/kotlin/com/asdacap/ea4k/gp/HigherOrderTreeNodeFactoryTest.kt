package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.HigherOrderNodeType.Companion.higherOrderFromKType
import com.asdacap.ea4k.gp.HigherOrderTreeNodeFactory.createConstantTreeNode
import com.asdacap.ea4k.gp.HigherOrderTreeNodeFactory.fromArgs
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class HigherOrderTreeNodeFactoryTest {

    fun primitive(n1: Int, n2: Int): Int {
        return n1 + n2
    }

    fun lazyPrimitive(input: Array<BaseTreeNode<*>>): Int {
        return 0
    }

    @Test
    fun testBasicFunctionCall() {
        val arg1Factory = fromArgs<Int>(0)
        val arg2Factory = fromArgs<Int>(1)
        val arg1 = arg1Factory.createNode(listOf())
        val arg2 = arg2Factory.createNode(listOf())
        val ctx = CallCtx(arrayOf(2, 3))

        val factory = HigherOrderTreeNodeFactory.fromFunction(this::primitive)
        assertTrue(higherOrderFromKType(typeOf<Int>()).isAssignableTo(factory.returnType))
        val node = factory.createNode(listOf(arg1, arg2))
        assertEquals(node.evaluate()(ctx), 5)
    }

    @Test
    fun testCommonBehaviour() {
        val factory = HigherOrderTreeNodeFactory.fromFunction(this::primitive)
        testCommonNodeBehaviour(factory, listOf(createConstantTreeNode(0), createConstantTreeNode(1)))
    }
}