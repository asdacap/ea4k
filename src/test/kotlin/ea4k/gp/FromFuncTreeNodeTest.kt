package ea4k.gp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class FromFuncTreeNodeTest {

    class TestNodeType(val value: Int, override val children: MutableList<BaseTreeNode<*>>): BaseTreeNode<Int>() {

        override fun call(ctx: CallCtx): Int {
            return value
        }

        override fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
            return otherTree is TestNodeType && value == otherTree.value
        }

        override fun clone(): BaseTreeNode<Int> {
            return TestNodeType(value, children)
        }

        override val returnType: KType = typeOf<Int>()
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
            val iteration = n1.iterateAllWithParentAndIndex();
            assertEquals(0, iteration.size)
        }

        @Test
        fun testNestedTreeNode() {
            val n1 = TestNodeType(2, mutableListOf())
            val n2 = TestNodeType(1, mutableListOf(n1))
            val n3 = TestNodeType(0, mutableListOf(n2))

            val iteration = n3.iterateAllWithParentAndIndex();
            assertEquals(2, iteration.size)
            assertSame(n2, iteration[0].first)
            assertSame(n3, iteration[1].first)
        }
    }

}