package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.HigherOrderNodeType.Companion.higherOrderFromKType
import com.asdacap.ea4k.gp.HigherOrderTreeNodeFactory.Companion.fromArgs
import com.asdacap.ea4k.gp.Utils.createConstantTreeNode
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
        val ctx = HigherOrderTreeNodeFactory.CallCtx(arrayOf(2, 3))

        val factory = HigherOrderTreeNodeFactory.fromFunction(this::primitive)
        assertEquals(higherOrderFromKType(typeOf<Int>()), factory.returnType)
        val node = factory.createNode(listOf(arg1, arg2))
        assertEquals(node.call()(ctx), 5)
    }

    /*
    @Test
    fun testCommonBehaviour() {
        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        testCommonNodeBehaviour(factory, listOf(createConstantTreeNode(0), createConstantTreeNode(1)))
    }

    @Test
    fun testMainConstructorDetectReturnTypeCorrectly() {
        val factory = FromFuncTreeNodeFactory(::lazyPrimitive, listOf())
        assertEquals(typeOf<Int>(), factory.returnType)
    }

    @Test
    fun testMainConstructorDetectReturnTypeCorrectlyWithClosure() {
        val factory = FromFuncTreeNodeFactory({ ctx, childs ->
            1f
        }, listOf())
        assertEquals(typeOf<Float>(), factory.returnType)
    }

    @Test
    fun testConstantDetectReturnTypeCorrectly() {
        val factory = FromFuncTreeNodeFactory.fromConstant(true)
        assertEquals(typeOf<Boolean>(), factory.returnType)
    }

    @Test
    fun testArgsIsNotPure() {
        val factory = fromArgs<Any>(0, typeOf<Int>())
        val node = factory.createNode(listOf())
        assertFalse(node.isPure)
        assertFalse(node.isSubtreeConstant)
    }

    @Test
    fun testReplaceNode() {
        val arg1Factory = fromArgs<Int>(0)
        val arg2Factory = fromArgs<Int>(1)
        val arg3Factory = fromArgs<Int>(2)
        val arg1 = arg1Factory.createNode(listOf())
        val arg2 = arg2Factory.createNode(listOf())
        val arg3 = arg3Factory.createNode(listOf())

        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        val node = factory.createNode(listOf(arg1, arg2))
        val node2 = factory.createNode(listOf(node, arg3))

        assertEquals(9, node2.call(CallCtx(arrayOf(2, 3, 4))))
        assertEquals(10, node2.replaceChild(arg1, arg2).call(CallCtx(arrayOf(2, 3, 4))))
    }

    @Test
    fun testSize() {
        val arg1Factory = fromArgs<Int>(0)
        val arg2Factory = fromArgs<Int>(1)
        val arg3Factory = fromArgs<Int>(2)
        val arg1 = arg1Factory.createNode(listOf())
        val arg2 = arg2Factory.createNode(listOf())
        val arg3 = arg3Factory.createNode(listOf())

        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        val node = factory.createNode(listOf(arg1, arg2))
        val node2 = factory.createNode(listOf(node, arg3))
        assertEquals(5, node2.size)
    }

    @Test
    fun testOptimizer() {
        val arg1Factory = fromArgs<Int>(0)
        val arg2Factory = fromArgs<Int>(1)
        val arg3Factory = fromArgs<Int>(2)
        val arg1 = arg1Factory.createNode(listOf())
        val arg2 = arg2Factory.createNode(listOf())
        val arg3 = arg3Factory.createNode(listOf())

        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
            .withOptimizer {
                createConstantTreeNode(99)
            }
        val node = factory.createNode(listOf(arg1, arg2))
        val node2 = factory.createNode(listOf(node, arg3))
        val optimized = node2.optimizeForEvaluation()
        assertEquals(99, optimized.call(CallCtx()))
    }
     */
}