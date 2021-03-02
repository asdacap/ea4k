package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.Utils.createConstantTreeNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class FromFuncTreeNodeFactoryTest {

    fun primitive(n1: Int, n2: Int): Int {
        return n1 + n2
    }

    fun lazyPrimitive(input: Array<Any>): Int {
        return 0
    }

    @Test
    fun testBasicFunctionCall() {
        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        assertEquals(KotlinNodeType(typeOf<Int>()), factory.returnType)
        val node = factory.createNode(listOf(createConstantTreeNode(2), createConstantTreeNode(3)))
        assertEquals(node.call(), 5)
    }

    @Test
    fun testCommonBehaviour() {
        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        testCommonNodeBehaviour(factory, listOf(createConstantTreeNode(0), createConstantTreeNode(1)))
    }

    @Test
    fun testMainConstructorDetectReturnTypeCorrectly() {
        val factory = FromFuncTreeNodeFactory(::lazyPrimitive, listOf())
        assertEquals(KotlinNodeType(typeOf<Int>()), factory.returnType)
    }

    @Test
    fun testMainConstructorDetectReturnTypeCorrectlyWithClosure() {
        val factory = FromFuncTreeNodeFactory({ inputs ->
            1f
        }, listOf())
        assertEquals(KotlinNodeType(typeOf<Float>()), factory.returnType)
    }

    @Test
    fun testConstantDetectReturnTypeCorrectly() {
        val factory = FromFuncTreeNodeFactory.fromConstant(true)
        assertEquals(KotlinNodeType(typeOf<Boolean>()), factory.returnType)
    }

    @Test
    fun testReplaceNode() {
        val arg1 = createConstantTreeNode(2)
        val arg2 = createConstantTreeNode(3)
        val arg3 = createConstantTreeNode(4)

        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        val node = factory.createNode(listOf(arg1, arg2))
        val node2 = factory.createNode(listOf(node, arg3))

        assertEquals(9, node2.call())
        assertEquals(10, node2.replaceChild(arg1, arg2).call())
    }

    @Test
    fun testSize() {
        val arg1 = createConstantTreeNode(2)
        val arg2 = createConstantTreeNode(3)
        val arg3 = createConstantTreeNode(4)

        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        val node = factory.createNode(listOf(arg1, arg2))
        val node2 = factory.createNode(listOf(node, arg3))
        assertEquals(5, node2.size)
    }
}