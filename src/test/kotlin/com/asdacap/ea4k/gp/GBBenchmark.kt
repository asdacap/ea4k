package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.Utils.createConstantTreeNode
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
import kotlin.system.measureTimeMillis

class GBBenchmark {
    val iteration = 500000

    fun primitive(n1: Int, n2: Int): Int {
        return n1 + n2
    }

    fun higherOrderPrimitive(n1: (HigherOrderTreeNodeFactory.CallCtx) -> Int, n2: (
        HigherOrderTreeNodeFactory.CallCtx) -> Int): (HigherOrderTreeNodeFactory.CallCtx) -> Int {
        return {
            n1(it) + n2(it)
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
            (1..iteration).forEach {
                cnode.call()
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
        var cnode = createConstantTreeNode({ ctx: HigherOrderTreeNodeFactory.CallCtx -> 1 } as (HigherOrderTreeNodeFactory.CallCtx) -> Int, typeOf<(HigherOrderTreeNodeFactory.CallCtx) -> Int>())
        (1..1000).forEach {
            cnode = factory.createNode(
                listOf(
                    cnode,
                    createConstantTreeNode({ ctx: HigherOrderTreeNodeFactory.CallCtx -> 1 } as (HigherOrderTreeNodeFactory.CallCtx) -> Int, typeOf<(HigherOrderTreeNodeFactory.CallCtx) -> Int>())
                ))
        }

        val time = measureTimeMillis {
            val func = cnode.call()
            (1..iteration).forEach {
                func(HigherOrderTreeNodeFactory.CallCtx())
            }
        }
        println("Higher order took $time ms")
    }

    @Test
    fun testHigherOrderIntegerAddBuildIn() {
        val factory = HigherOrderTreeNodeFactory.fromLazyFunction({ ctx, ins ->
            primitive(ins[0](ctx) as Int, ins[1](ctx) as Int)
        }, listOf())
        var cnode = HigherOrderTreeNodeFactory.fromLazyFunction({ ctx, chs -> 1 }, listOf(KotlinNodeType(typeOf<Int>()))).createNode(listOf())
        (1..1000).forEach {
            cnode = factory.createNode(listOf(cnode, createConstantTreeNode({ ctx: HigherOrderTreeNodeFactory.CallCtx -> 1 },
                typeOf<(HigherOrderTreeNodeFactory.CallCtx) -> Int>())))
        }

        val time = measureTimeMillis {
            val func = cnode.call()
            (1..iteration).forEach {
                func(HigherOrderTreeNodeFactory.CallCtx())
            }
        }
        println("Build in higher order took $time ms")
    }

    @Test
    fun testHigherOrderWithFunctionMaker() {
        val factory = HigherOrderTreeNodeFactory.fromFunctionMaker({
            val in1 = it[0] as (HigherOrderTreeNodeFactory.CallCtx) -> Int
            val in2 = it[1] as (HigherOrderTreeNodeFactory.CallCtx) -> Int
            {
                primitive(in1(it), in2(it))
            }
        }, listOf(KotlinNodeType(typeOf<Int>())))
        var cnode = HigherOrderTreeNodeFactory.createConstantTreeNode(1)
        (1..1000).forEach {
            cnode = factory.createNode(listOf(cnode, HigherOrderTreeNodeFactory.createConstantTreeNode(1)));
        }

        val time = measureTimeMillis {
            val func = cnode.call()
            (1..iteration).forEach {
                func(HigherOrderTreeNodeFactory.CallCtx())
            }
        }
        println("Function maker higher order took $time ms")
    }

    @Test
    fun testHigherOrderWithFunctionFactory() {
        val factory = HigherOrderTreeNodeFactory.fromBinaryFunction(::primitive)
        var cnode = HigherOrderTreeNodeFactory.createConstantTreeNode(1)
        (1..1000).forEach {
            cnode = factory.createNode(listOf(cnode, HigherOrderTreeNodeFactory.createConstantTreeNode(1)));
        }

        val time = measureTimeMillis {
            val func = cnode.call()
            (1..iteration).forEach {
                func(HigherOrderTreeNodeFactory.CallCtx())
            }
        }
        println("From func higher order took $time ms")
    }
}