package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.Utils.createConstantTreeNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class FromFuncTreeNodeTest {

    fun primitive(n1: Int, n2: Int): Int {
        return n1 + n2
    }

    fun lazyPrimitive(item: Array<Any>): Int {
        return 0
    }

    @Test
    fun testBasicFunctionCall() {
        val factory = FromFuncTreeNode.factoryFromFunction(this::primitive)
        assertEquals(KotlinNodeType(typeOf<Int>()), factory.returnType)
        val node = factory.createNode(listOf(createConstantTreeNode(2), createConstantTreeNode(3)))
        assertEquals(node.evaluate(), 5)
    }

    @Test
    fun testCommonBehaviour() {
        val factory = FromFuncTreeNode.factoryFromFunction(this::primitive)
        testCommonNodeBehaviour(factory, listOf(createConstantTreeNode(0), createConstantTreeNode(1)))
    }

    @Test
    fun testMainConstructorDetectReturnTypeCorrectly() {
        val factory = FromFuncTreeNode.Factory(::lazyPrimitive)
        assertEquals(KotlinNodeType(typeOf<Int>()), factory.returnType)
    }

    @Test
    fun testMainConstructorDetectReturnTypeCorrectlyWithClosure() {
        val factory = FromFuncTreeNode.Factory({ 1 })
        assertEquals(KotlinNodeType(typeOf<Float>()), factory.returnType)
    }

    @Test
    fun testConstantDetectReturnTypeCorrectly() {
        val factory = FromFuncTreeNode.factoryFromConstant(true)
        assertEquals(KotlinNodeType(typeOf<Boolean>()), factory.returnType)
    }

    @Test
    fun testReplaceNode() {
        val arg1 = createConstantTreeNode(2)
        val arg2 = createConstantTreeNode(3)
        val arg3 = createConstantTreeNode(4)

        val factory = FromFuncTreeNode.factoryFromFunction(this::primitive)
        val node = factory.createNode(listOf(arg1, arg2))
        val node2 = factory.createNode(listOf(node, arg3))

        assertEquals(9, node2.evaluate())
        assertEquals(10, node2.replaceChild(arg1, arg2).evaluate())
    }

    @Test
    fun testSize() {
        val arg1 = createConstantTreeNode(2)
        val arg2 = createConstantTreeNode(3)
        val arg3 = createConstantTreeNode(4)

        val factory = FromFuncTreeNode.factoryFromFunction(this::primitive)
        val node = factory.createNode(listOf(arg1, arg2))
        val node2 = factory.createNode(listOf(node, arg3))
        assertEquals(5, node2.size)
    }
}