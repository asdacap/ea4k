package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.HigherOrderNodeType.Companion.higherOrderFromKType
import sun.reflect.generics.tree.ReturnType
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
object HigherOrderTreeNodeFactory {

    /**
     * Create a higher order tree node factory from a lambda that itself accept a list of other function.
     * This should be the fastest method. It also allow the handling function to skip some child evaluation
     * for example, for an AND predicate, if one child already get a false, the other child no longer need to be evaluated.
     */
    inline fun <reified R> fromFunctionMaker(
        crossinline func: (Array<(CallCtx) -> Any>) -> (CallCtx) -> R,
        args: List<NodeType>,
        returnType: NodeType = higherOrderFromKType(typeOf<R>())
    ): FromFuncTreeNodeFactory<(CallCtx) -> R> {
        return FromFuncTreeNodeFactory({ input ->
            input.map {
                (it as (CallCtx) -> Any)
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
    ): FromFuncTreeNodeFactory<(CallCtx) -> R> {
        return FromFuncTreeNodeFactory({ input ->
            val arg1 = input[0] as (CallCtx) -> I1
            val arg2 = input[1] as (CallCtx) -> I2

            {
                func(arg1(it), arg2(it))
            }
        }, args, returnType)
    }

    /**
     * Create a higher order tree node factory from that uses the passed in function.
     * The resulting function have some overhead.
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
     * Create a higher order tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R> fromArgs(argIdx: Int): FromFuncTreeNodeFactory<(CallCtx) -> R> {
        return FromFuncTreeNodeFactory({ input ->
            { ctx ->
                ctx.args[argIdx] as R
            }
        }, listOf(), higherOrderFromKType(typeOf<R>()))
    }

    /**
     * Create a higher order tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R: Any> fromGenerator(crossinline generator: () -> R): TreeNodeFactory<(CallCtx) -> R> {
        return HigherOrderGeneratorTerminalFactory({ generator() }, higherOrderFromKType(typeOf<R>()))
    }
}
