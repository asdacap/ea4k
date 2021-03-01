package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.Utils.createConstantTreeNode
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
import kotlin.system.measureTimeMillis

class GBBenchmark {

    fun primitive(n1: Int, n2: Int): Int {
        return n1 + n2
    }

    fun higherOrderPrimitive(n1: () -> Int, n2: () -> Int): () -> Int {
        return {
            n1() + n2()
        }
    }

    @Test
    fun testIntegerAdd() {
        val factory = FromFuncTreeNodeFactory.fromFunction(this::primitive)
        var cnode = createConstantTreeNode(1)
        (1..1000).forEach {
            cnode = factory.createNode(listOf(cnode, createConstantTreeNode(1)))
        }

        val time = measureTimeMillis {
            (1..100000).forEach {
                cnode.call(CallCtx())
            }
        }
        println("Original took $time ms")
    }

    fun constantFactory(): Int {
        return 1
    }

    @Test
    fun testHigherOrderIntegerAdd() {
        val factory = FromFuncTreeNodeFactory.fromFunction(this::higherOrderPrimitive)
        var cnode = createConstantTreeNode({ 1 } as () -> Int, typeOf<() -> Int>())
        (1..1000).forEach {
            cnode = factory.createNode(listOf(cnode, createConstantTreeNode({ 1 } as () -> Int, typeOf<() -> Int>())))
        }

        val time = measureTimeMillis {
            val func = cnode.call(CallCtx())
            (1..100000).forEach {
                func()
            }
        }
        println("Higher order took $time ms")
    }
}