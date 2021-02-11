package ea4k.gp

import ea4k.gp.FromFuncTreeNodeFactory.Companion.fromArgs
import ea4k.gp.Utils.createConstantTreeNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class FromFuncTreeNodeFactoryTest {

    fun primitive(n1: Int, n2: Int): Int {
        return n1 + n2
    }

    fun lazyPrimitive(callCtx: CallCtx, children: Array<BaseTreeNode<*>>): Int {
        return 0
    }

    @Test
    fun testCloneWillCloneChildAlso() {
        val arg1Factory = fromArgs<Int>(0)
        val arg2Factory = fromArgs<Int>(1)
        val arg1 = arg1Factory.createNode(listOf())
        val arg2 = arg2Factory.createNode(listOf())
        val ctx = CallCtx(arrayOf(2, 3))

        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        val node = factory.createNode(listOf(arg1, arg2)).clone()

        assertNotSame(arg1, node.children[0])
    }

    @Test
    fun testBasicFunctionCall() {
        val arg1Factory = fromArgs<Int>(0)
        val arg2Factory = fromArgs<Int>(1)
        val arg1 = arg1Factory.createNode(listOf())
        val arg2 = arg2Factory.createNode(listOf())
        val ctx = CallCtx(arrayOf(2, 3))

        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        assertEquals(typeOf<Int>(), factory.returnType)
        val node = factory.createNode(listOf(arg1, arg2))
        assertEquals(node.call(ctx), 5)
    }

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
}