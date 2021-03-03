package com.asdacap.ea4k.gp.function

import com.asdacap.ea4k.gp.*
import com.asdacap.ea4k.gp.Function
import com.asdacap.ea4k.gp.functional.FunctionGeneratorTerminalFactory
import com.asdacap.ea4k.gp.functional.FunctionNodeType
import com.asdacap.ea4k.gp.functional.FunctionNodeType.Companion.functionalNodeTypeFromKType
import kotlin.reflect.KCallable
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.reflect
import kotlin.reflect.typeOf

/**
 * A function tree node is basically a tree node that evaluate into a function, which in turn can be called.
 * The TreeNode then basically create function from other function.
 * The resulting function may have little to no reflection overhead which makes it really fast.
 *
 * The resulting function will accept a single parameter, which is of type CallCtx. The input of the function
 * can then be put in the CallCtx.
 */
object FunctionTreeNodeConstructors {

    /**
     * Create a function tree node factory from a lambda that itself accept a list of other function.
     * This should be the fastest method. It also allow the handling function to skip some child evaluation
     * for example, for an AND predicate, if one child already get a false, the other child no longer need to be evaluated.
     */
    inline fun <reified R> fromFunctionMaker(
        crossinline func: (Array<Function<Any>>) -> Function<R>,
        args: List<NodeType>,
        returnType: NodeType = functionalNodeTypeFromKType(typeOf<R>())
    ): FromFuncTreeNodeFactory<Function<R>> {
        return FromFuncTreeNodeFactory({ input ->
            input.map {
                (it as Function<Any>)
            }.toTypedArray().let { inputVals ->
                func(inputVals)
            }
        }, returnType, args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from a lambda that will be used in the generated function.
     * This is a special case with special implementation because it is a lot faster than fromFunction
     */
    inline fun <reified R, I1, I2> fromBinaryFunction(
        noinline func: (I1, I2) -> R,
        args: List<NodeType> = func.reflect()!!.parameters.map { functionalNodeTypeFromKType(it.type) },
        returnType: NodeType = functionalNodeTypeFromKType(typeOf<R>())
    ): FromFuncTreeNodeFactory<Function<R>> {
        return FromFuncTreeNodeFactory({ input ->
            val arg1 = input[0] as Function<I1>
            val arg2 = input[1] as Function<I2>

            Function { ctx: CallCtx ->
                func(arg1.call(ctx), arg2.call(ctx))
            }
        }, returnType, args)
    }

    /**
     * Create a function tree node factory from that uses the passed in function.
     * The resulting function have some overhead.
     */
    inline fun <reified R> fromFunction(func: KCallable<R>): FromFuncTreeNodeFactory<Function<R>> {
        val args = func.parameters.map { functionalNodeTypeFromKType(it.type) }
        return FromFuncTreeNodeFactory({ input ->
            input.map {
                (it as Function<Any>)
            }.toTypedArray().let { inputValProducer ->
                arrayOfNulls<Any>(inputValProducer.size).let { inputVals ->
                    Function { callCtx: CallCtx ->
                        var i = 0
                        while (i < args.size) {
                            inputVals[i] = inputValProducer[i].call(callCtx)
                            i++
                        }
                        func.call(*inputVals)
                    }
                }
            }
        }, functionalNodeTypeFromKType(typeOf<R>()), args)
    }

    /**
     * Create a function tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R> fromArgs(argIdx: Int): FromFuncTreeNodeFactory<Function<R>> {
        return FromFuncTreeNodeFactory({ input ->
            Function { ctx ->
                ctx.args[argIdx] as R
            }
        }, functionalNodeTypeFromKType(typeOf<R>()))
    }

    /**
     * Create a tree node factory that creates a function tree node that always return the constant specified
     */
    inline fun <reified R> createConstantProducer(constant: R): FromFuncTreeNodeFactory<Function<R>> {
        return fromFunctionMaker({ Function { constant } }, listOf())
    }

    fun <R> createConstantTreeNode(constant: R, type: NodeType = FunctionNodeType(KotlinNodeType(constant!!::class.createType())))
            : BaseTreeNode<Function<R>> {
        return FromFuncTreeNodeFactory({ input ->
            Function { constant }
        }, type).createNode(listOf())
    }

    /**
     * Create a function tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R: Any> fromGenerator(noinline generator: () -> R): TreeNodeFactory<Function<R>> {
        return FunctionGeneratorTerminalFactory(generator, functionalNodeTypeFromKType(typeOf<R>()))
    }
}
