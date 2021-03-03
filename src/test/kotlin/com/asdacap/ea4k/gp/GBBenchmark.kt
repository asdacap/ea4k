package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.Utils.createConstantTreeNode
import com.asdacap.ea4k.gp.higherorder.HigherOrderTreeNodeConstructors
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
import kotlin.system.measureTimeMillis

class GBBenchmark {
    val iteration = 50000

    fun primitive(n1: Int, n2: Int): Int {
        return n1 + n2
    }

    fun higherOrderPrimitive(n1: (CallCtx) -> Int, n2: (
        CallCtx
    ) -> Int): (CallCtx) -> Int {
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
                cnode.evaluate()
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
        var cnode = createConstantTreeNode({ ctx: CallCtx -> 1 } as (CallCtx) -> Int, KotlinNodeType(typeOf<(CallCtx) -> Int>()))
        (1..1000).forEach {
            cnode = factory.createNode(
                listOf(
                    cnode,
                    createConstantTreeNode({ ctx: CallCtx -> 1 } as (CallCtx) -> Int, KotlinNodeType(typeOf<(CallCtx) -> Int>()))
                ))
        }

        val time = measureTimeMillis {
            val func = cnode.evaluate()
            (1..iteration).forEach {
                func(CallCtx())
            }
        }
        println("Higher order took $time ms")
    }

    @Test
    fun testHigherOrderWithFunctionMaker() {
        val factory = HigherOrderTreeNodeConstructors.fromFunctionMaker({
            val in1 = it[0] as CallCtxFunction<Int>
            val in2 = it[1] as CallCtxFunction<Int>
            CallCtxFunction {
                primitive(in1.call(it), in2.call(it))
            }
        }, listOf(KotlinNodeType(typeOf<Int>())))
        var cnode = HigherOrderTreeNodeConstructors.createConstantTreeNode(1)
        (1..1000).forEach {
            cnode = factory.createNode(listOf(cnode, HigherOrderTreeNodeConstructors.createConstantTreeNode(1)));
        }

        val time = measureTimeMillis {
            val func = cnode.evaluate()
            (1..iteration).forEach {
                func.call(CallCtx())
            }
        }
        println("Function maker higher order took $time ms")
    }

    @Test
    fun testHigherOrderWithFunctionFactory() {
        val factory = HigherOrderTreeNodeConstructors.fromBinaryFunction(::primitive)
        var cnode = HigherOrderTreeNodeConstructors.createConstantTreeNode(1)
        (1..1000).forEach {
            cnode = factory.createNode(listOf(cnode, HigherOrderTreeNodeConstructors.createConstantTreeNode(1)));
        }

        val time = measureTimeMillis {
            val func = cnode.evaluate()
            (1..iteration).forEach {
                func.call(CallCtx())
            }
        }
        println("From func higher order took $time ms")
    }
}