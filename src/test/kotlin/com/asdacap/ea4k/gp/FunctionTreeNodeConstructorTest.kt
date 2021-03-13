package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.createConstantTreeNode
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromArgs
import com.asdacap.ea4k.gp.functional.CallCtx
import com.asdacap.ea4k.gp.functional.ConstantNodeFunction
import com.asdacap.ea4k.gp.functional.FunctionNodeType.Companion.functionalNodeTypeFromKType
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromConstant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class FunctionTreeNodeConstructorTest {

    fun primitive(n1: Int, n2: Int): Int {
        return n1 + n2
    }

    fun lazyPrimitive(input: Array<TreeNode<*>>): Int {
        return 0
    }

    @Test
    fun testBasicFunctionCall() {
        val arg1Factory = fromArgs<Int>(0)
        val arg2Factory = fromArgs<Int>(1)
        val arg1 = arg1Factory.createNode(listOf())
        val arg2 = arg2Factory.createNode(listOf())
        val ctx = CallCtx(arrayOf(2, 3))

        val factory = FunctionTreeNodeConstructors.fromKCallable(this::primitive)
        assertTrue(functionalNodeTypeFromKType(typeOf<Int>()).isAssignableTo(factory.returnType))
        val node = factory.createNode(listOf(arg1, arg2))
        assertEquals(node.evaluate().call(ctx), 5)
    }

    @Test
    fun testCommonBehaviour() {
        val factory = FunctionTreeNodeConstructors.fromKCallable(this::primitive)
        testCommonNodeBehaviour(factory, listOf(createConstantTreeNode(0), createConstantTreeNode(1)))
    }

    @Test
    fun testConstantOptimization() {
        val constant1Factory = fromConstant(2)
        val constant2Factory = fromConstant(3)
        val constant1 = constant1Factory.createNode(listOf())
        val constant2 = constant2Factory.createNode(listOf())

        val factory = FunctionTreeNodeConstructors.fromKCallable(this::primitive)
        assertTrue(functionalNodeTypeFromKType(typeOf<Int>()).isAssignableTo(factory.returnType))
        val node = factory.createNode(listOf(constant1, constant2))
        val evaluated = node.evaluate()
        assertTrue(evaluated is ConstantNodeFunction && evaluated.constant == 5)
        assertEquals(node.evaluate().call(CallCtx()), 5)
    }
}