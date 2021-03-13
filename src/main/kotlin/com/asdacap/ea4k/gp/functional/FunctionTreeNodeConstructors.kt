package com.asdacap.ea4k.gp.functional

import com.asdacap.ea4k.gp.FromFuncTreeNode
import com.asdacap.ea4k.gp.KotlinNodeType
import com.asdacap.ea4k.gp.NodeType
import com.asdacap.ea4k.gp.TreeNode
import com.asdacap.ea4k.gp.TreeNodeFactory
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
        crossinline func: (Array<NodeFunction<Any>>) -> NodeFunction<R>,
        args: List<NodeType>,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
    ): TreeNodeFactory<NodeFunction<R>> {
        return FromFuncTreeNode.Factory({ input ->
            input.map {
                (it as NodeFunction<Any>)
            }.toTypedArray().let { inputVals ->
                func(inputVals)
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from a lambda that will be used in the generated function.
     * This implementation does not use reflection.
     */
    inline fun <reified R, I> fromFunction(
        noinline func: (I) -> R,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        args: List<NodeType> = func.reflect()!!.parameters.map { NodeType.fromKotlinNodeType(it.type) },
    ): TreeNodeFactory<NodeFunction<R>> {
        return FromFuncTreeNode.Factory({ input ->
            val arg1 = input[0] as NodeFunction<I>

            NodeFunction { ctx: CallCtx ->
                func(arg1.call(ctx))
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from a lambda that will be used in the generated function.
     * This implementation does not use reflection.
     */
    inline fun <reified R, I1, I2> fromFunction(
        noinline func: (I1, I2) -> R,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        args: List<NodeType> = func.reflect()!!.parameters.map { NodeType.fromKotlinNodeType(it.type) },
    ): TreeNodeFactory<NodeFunction<R>> {
        return FromFuncTreeNode.Factory({ input ->
            val arg1 = input[0] as NodeFunction<I1>
            val arg2 = input[1] as NodeFunction<I2>

            NodeFunction { ctx: CallCtx ->
                func(arg1.call(ctx), arg2.call(ctx))
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from a lambda that will be used in the generated function.
     * This implementation does not use reflection.
     */
    inline fun <reified R, I1, I2, I3> fromFunction(
        noinline func: (I1, I2, I3) -> R,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        args: List<NodeType> = func.reflect()!!.parameters.map { NodeType.fromKotlinNodeType(it.type) },
    ): TreeNodeFactory<NodeFunction<R>> {
        return FromFuncTreeNode.Factory({ input ->
            val arg1 = input[0] as NodeFunction<I1>
            val arg2 = input[1] as NodeFunction<I2>
            val arg3 = input[1] as NodeFunction<I3>

            NodeFunction { ctx: CallCtx ->
                func(arg1.call(ctx), arg2.call(ctx), arg3.call(ctx))
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from that uses the passed in function.
     * The resulting function have some overhead.
     */
    inline fun <reified R> fromKCallable(
        func: KCallable<R>,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        args: List<NodeType> = func.parameters.map { NodeType.fromKotlinNodeType(it.type) },
    ): TreeNodeFactory<NodeFunction<R>> {
        val args = func.parameters.map { functionalNodeTypeFromKType(it.type) }
        return FromFuncTreeNode.Factory({ input ->
            input.map {
                (it as NodeFunction<Any>)
            }.toTypedArray().let { inputValProducer ->
                arrayOfNulls<Any>(inputValProducer.size).let { inputVals ->
                    NodeFunction { callCtx: CallCtx ->
                        var i = 0
                        while (i < args.size) {
                            inputVals[i] = inputValProducer[i].call(callCtx)
                            i++
                        }
                        func.call(*inputVals)
                    }
                }
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R> fromArgs(argIdx: Int, nodeType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>())): TreeNodeFactory<NodeFunction<R>> {
        return FromFuncTreeNode.Factory({ input ->
            NodeFunction { ctx ->
                ctx.args[argIdx] as R
            }
        }, FunctionNodeType(nodeType))
    }

    /**
     * Create a tree node factory that creates a function tree node that always return the constant specified
     */
    inline fun <reified R> createConstantProducer(constant: R): TreeNodeFactory<NodeFunction<R>> {
        return fromFunctionMaker({ NodeFunction { constant } }, listOf())
    }

    fun <R> createConstantTreeNode(constant: R, type: NodeType = FunctionNodeType(KotlinNodeType(constant!!::class.createType())))
            : TreeNode<NodeFunction<R>> {
        return FromFuncTreeNode.Factory({ input ->
            NodeFunction { constant }
        }, type).createNode(listOf())
    }

    /**
     * Create a function tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R: Any> fromGenerator(noinline generator: () -> R): TreeNodeFactory<NodeFunction<R>> {
        return GeneratorTreeNode.Factory(generator, returnType = functionalNodeTypeFromKType(typeOf<R>()))
    }
}
