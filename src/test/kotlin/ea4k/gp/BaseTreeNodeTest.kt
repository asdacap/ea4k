package ea4k.gp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class BaseTreeNodeTest {

    class TestNodeType(val value: Int, override val children: List<BaseTreeNode<*>>): BaseTreeNode<Int>() {

        override fun call(ctx: CallCtx): Int {
            return value
        }

        override fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
            return otherTree is TestNodeType && value == otherTree.value
        }

        override val returnType: KType = typeOf<Int>()

        override fun replaceChildren(newChildren: List<BaseTreeNode<*>>): BaseTreeNode<Int> {
            return TestNodeType(value, newChildren)
        }
    }

    @Nested
    class TestIsEffectivelyTheSame() {

        @Test
        fun testSameNode() {
            val n1 = TestNodeType(0, mutableListOf(TestNodeType(1, mutableListOf())))
            val n2 = TestNodeType(0, mutableListOf(TestNodeType(1, mutableListOf())))
            assertTrue(n1.isSubtreeEffectivelySame(n2))
        }

        @Test
        fun testDifferentNode() {
            val n1 = TestNodeType(0, mutableListOf())
            val n2 = TestNodeType(1, mutableListOf())
            assertFalse(n1.isSubtreeEffectivelySame(n2))
        }

        @Test
        fun testDifferentChildNode() {
            val n1 = TestNodeType(0, mutableListOf(TestNodeType(1, mutableListOf())))
            val n2 = TestNodeType(0, mutableListOf(TestNodeType(0, mutableListOf())))
            assertFalse(n1.isSubtreeEffectivelySame(n2))
        }
    }

    @Nested
    class TestSize() {

        @Test
        fun testSingleNode() {
            val n1 = TestNodeType(0, mutableListOf())
            assertEquals(1, n1.size)
        }

        @Test
        fun testNestedTreeNode() {
            val n1 = TestNodeType(0, mutableListOf(TestNodeType(0, mutableListOf(TestNodeType(0, mutableListOf())))))
            assertEquals(3, n1.size)
        }
    }

    @Nested
    class TestIteration() {

        @Test
        fun testSingleNode() {
            val n1 = TestNodeType(0, mutableListOf())
            val iteration = n1.iterateAll();
            assertEquals(1, iteration.size)
        }

        @Test
        fun testNestedTreeNode() {
            val n1 = TestNodeType(2, mutableListOf())
            val n2 = TestNodeType(1, mutableListOf(n1))
            val n3 = TestNodeType(0, mutableListOf(n2))

            val iteration = n3.iterateAll();
            assertEquals(3, iteration.size)
            assertSame(n1, iteration[0])
            assertSame(n2, iteration[1])
            assertSame(n3, iteration[2])
        }
    }

    @Nested
    class TestReplaceChild() {

        @Test
        fun testReplaceChild() {
            val n1 = TestNodeType(2, mutableListOf())
            val n2 = TestNodeType(1, mutableListOf())
            val n3 = TestNodeType(0, mutableListOf(n1, n2))
            val n4 = n3.replaceChild(1, n1)

            val iteration = n4.iterateAll();
            assertNotSame(n3, n4)
            assertSame(n1, iteration[0])
            assertSame(n1, iteration[1])
            assertSame(n4, iteration[2])
        }

        @Test
        fun testNoReplacement() {
            val n1 = TestNodeType(2, mutableListOf())
            val n2 = TestNodeType(1, mutableListOf())
            val n3 = TestNodeType(0, mutableListOf(n1, n1))
            val n4 = n3.replaceChild(n2, n1)

            assertSame(n3, n4)
        }
    }
}