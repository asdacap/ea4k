package com.asdacap.ea4k.gp.higherorder

import com.asdacap.ea4k.gp.*
import com.asdacap.ea4k.gp.higherorder.HigherOrderNodeType.Companion.higherOrderFromKType
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

/**
 * A HigherOrder tree node is basically a tree node that evaluate into another tree node... but not in this case.
 * Its a TreeNode that evaluate into a function which can be called. The TreeNode then basically create function
 * from other function. The resulting function may have little to no reflection overhead which makes it really fast.
 *
 * The resulting function will accept a single parameter, which is of type CallCtx. The input of the function
 * can then be put in the CallCtx.
 */
object HigherOrderTreeNodeConstructors {

    /**
     * Create a higher order tree node factory from a lambda that itself accept a list of other function.
     * This should be the fastest method. It also allow the handling function to skip some child evaluation
     * for example, for an AND predicate, if one child already get a false, the other child no longer need to be evaluated.
     */
    inline fun <reified R> fromFunctionMaker(
        crossinline func: (Array<CallCtxFunction<Any>>) -> CallCtxFunction<R>,
        args: List<NodeType>,
        returnType: NodeType = higherOrderFromKType(typeOf<R>())
    ): FromFuncTreeNodeFactory<CallCtxFunction<R>> {
        return FromFuncTreeNodeFactory({ input ->
            input.map {
                (it as CallCtxFunction<Any>)
            }.toTypedArray().let { inputVals ->
                func(inputVals)
            }
        }, args.map { HigherOrderNodeType(it) }, returnType)
    }

    /**
     * Create a higher order tree node factory from a lambda that will be used in the generated function.
     * This is a special case with special implementation because it is a lot faster than fromFunction
     */
    inline fun <reified R, I1, I2> fromBinaryFunction(
        noinline func: (I1, I2) -> R,
        args: List<NodeType> = func.reflect()!!.parameters.map { higherOrderFromKType(it.type) },
        returnType: NodeType = higherOrderFromKType(typeOf<R>())
    ): FromFuncTreeNodeFactory<CallCtxFunction<R>> {
        return FromFuncTreeNodeFactory({ input ->
            val arg1 = input[0] as CallCtxFunction<I1>
            val arg2 = input[1] as CallCtxFunction<I2>

            CallCtxFunction { ctx: CallCtx ->
                func(arg1.call(ctx), arg2.call(ctx))
            }
        }, args, returnType)
    }

    /**
     * Create a higher order tree node factory from that uses the passed in function.
     * The resulting function have some overhead.
     */
    inline fun <reified R> fromFunction(func: KCallable<R>): FromFuncTreeNodeFactory<CallCtxFunction<R>> {
        val args = func.parameters.map { higherOrderFromKType(it.type) }
        return FromFuncTreeNodeFactory({ input ->
            input.map {
                (it as CallCtxFunction<Any>)
            }.toTypedArray().let { inputValProducer ->
                arrayOfNulls<Any>(inputValProducer.size).let { inputVals ->
                    CallCtxFunction{ callCtx: CallCtx ->
                        var i = 0
                        while (i < args.size) {
                            inputVals[i] = inputValProducer[i].call(callCtx)
                            i++
                        }
                        func.call(*inputVals)
                    }
                }
            }
        }, args, higherOrderFromKType(typeOf<R>()))
    }

    /**
     * Create a higher order tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R> fromArgs(argIdx: Int): FromFuncTreeNodeFactory<CallCtxFunction<R>> {
        return FromFuncTreeNodeFactory({ input ->
            CallCtxFunction { ctx ->
                ctx.args[argIdx] as R
            }
        }, listOf(), higherOrderFromKType(typeOf<R>()))
    }

    /**
     * Create a tree node factory that creates a higher order tree node that always return the constant specified
     */
    inline fun <reified R> createConstantProducer(constant: R): FromFuncTreeNodeFactory<CallCtxFunction<R>> {
        return fromFunctionMaker({ CallCtxFunction{ constant } }, listOf())
    }

    fun <R> createConstantTreeNode(constant: R, type: NodeType = HigherOrderNodeType(KotlinNodeType(constant!!::class.createType())))
            : BaseTreeNode<CallCtxFunction<R>> {
        return FromFuncTreeNodeFactory.TreeNode({ input ->
            CallCtxFunction { constant }
        }, type, listOf())
    }

    /**
     * Create a higher order tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R: Any> fromGenerator(crossinline generator: () -> R): TreeNodeFactory<CallCtxFunction<R>> {
        return HigherOrderGeneratorTerminalFactory({ generator() }, higherOrderFromKType(typeOf<R>()))
    }
}
