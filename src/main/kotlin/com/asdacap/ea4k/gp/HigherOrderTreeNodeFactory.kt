package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.HigherOrderNodeType.Companion.higherOrderFromKType
import kotlin.reflect.KCallable
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.reflect
import kotlin.reflect.typeOf

data class HigherOrderNodeType(val innerType: NodeType): NodeType {
    override fun isAssignableTo(otherNodeType: NodeType): Boolean {
        return otherNodeType is HigherOrderNodeType && innerType.isAssignableTo(otherNodeType.innerType)
    }

    companion object {
        fun higherOrderFromKType(ktype: KType): HigherOrderNodeType {
            return HigherOrderNodeType(KotlinNodeType(ktype))
        }
    }
}

class HigherOrderTreeNodeFactory {
    /**
     * Per tree evaluation, there is a context.
     * Notably, the context provide the arguments for the terminals.
     */
    class CallCtx(
        // Array instead of List for optimization reason
        val args: Array<Any> = arrayOf()
    )

    companion object {

        /**
         * Create a tree node factory from a kotlin KCallable. This will use reflection to detect the callable's
         * argument and return type.
         */
        fun <R> fromFunctionMaker(func: (Array<(CallCtx) -> Any>) -> (CallCtx) -> R, args: List<NodeType>): FromFuncTreeNodeFactory<(CallCtx) -> R> {
            return FromFuncTreeNodeFactory({ input ->
                input.map {
                    (it as (CallCtx) -> Any)
                }.toTypedArray().let { inputVals ->
                    func(inputVals)
                }
            }, args.map { HigherOrderNodeType(it) }, higherOrderFromKType(func.reflect()!!.returnType))
        }

        /**
         * Create a tree node factory from a kotlin KCallable. This will use reflection to detect the callable's
         * argument and return type.
         */
        fun <R> fromLazyFunction(func: (CallCtx, Array<(CallCtx) -> Any>) -> R, args: List<NodeType>): FromFuncTreeNodeFactory<(CallCtx) -> R> {
            return FromFuncTreeNodeFactory({ input ->
                input.map {
                    (it as (CallCtx) -> Any)
                }.toTypedArray().let { inputVals ->
                    { callCtx: CallCtx ->
                        func(callCtx, inputVals)
                    } as (CallCtx) -> R
                }
            }, args.map { HigherOrderNodeType(it) }, higherOrderFromKType(func.reflect()!!.returnType))
        }

        /**
         * Create a tree node factory from a kotlin KCallable. This will use reflection to detect the callable's
         * argument and return type.
         */
        inline fun <reified R> fromFunction(func: KCallable<R>): FromFuncTreeNodeFactory<(CallCtx) -> R> {
            val args = func.parameters.map { higherOrderFromKType(it.type) }
            return FromFuncTreeNodeFactory({ input ->
                input.map {
                    (it as (CallCtx) -> Any)
                }.toTypedArray().let { inputValProducer ->
                    arrayOfNulls<Any>(inputValProducer.size).let { inputVals ->
                        { callCtx: CallCtx ->
                            var i = 0
                            while (i < args.size) {
                                inputVals[i] = inputValProducer[i](callCtx)
                                i++
                            }
                            func.call(*inputVals)
                        }
                    }
                }
            }, args, higherOrderFromKType(typeOf<R>()))
        }

        /**
         * Create a tree node factory from a kotlin KCallable. This will use reflection to detect the callable's
         * argument and return type.
         */
        inline fun <reified R, I1, I2> fromBinaryFunction(
            noinline func: (I1, I2) -> R,
            args: List<NodeType> = func.reflect()!!.parameters.map { higherOrderFromKType(it.type) }
        ): FromFuncTreeNodeFactory<(CallCtx) -> R> {
            return FromFuncTreeNodeFactory({ input ->
                val arg1 = input[0] as (CallCtx) -> I1
                val arg2 = input[1] as (CallCtx) -> I2

                {
                    func(arg1(it), arg2(it))
                }
            }, args, higherOrderFromKType(typeOf<R>()))
        }

        /**
         * Create a tree node factory that fetch item from the call context.
         * This is basically the tree terminal that gets input.
         */
        inline fun <reified R> fromArgs(argIdx: Int): FromFuncTreeNodeFactory<(CallCtx) -> R> {
            return FromFuncTreeNodeFactory({ input ->
                { ctx ->
                    ctx.args[argIdx] as R
                }
            }, listOf(), higherOrderFromKType(typeOf<R>()))
        }

        fun <R> createConstantTreeNode(constant: R, type: KType = constant!!::class.createType()): BaseTreeNode<(CallCtx) -> R> {
            return FromFuncTreeNodeFactory.TreeNode({ input ->
                { constant }
            }, KotlinNodeType(type), listOf())
        }

        /**
         * Create a tree node factory that always return the same result.
         */
        inline fun <reified R> kotlinTypeRaiser(ktype: KType): FromFuncTreeNodeFactory<(CallCtx) -> R> {
            return FromFuncTreeNodeFactory({ input ->
                { input[0] as R }
            }, listOf(KotlinNodeType(ktype)), higherOrderFromKType(ktype))
        }
    }
}
