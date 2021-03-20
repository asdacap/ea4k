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
 * The resulting function will accept a single parameter, which is of type CallCtx. The input of the function tree
 * can then be put in the CallCtx. All constructors here will enclose the given NodeType into FunctionNodeType.
 */
object FunctionTreeNodeConstructors {

    /**
     * Create a function tree node factory from a lambda that itself accept a list of other function.
     * This should be the fastest method.
     * It also allow the handling function to skip some child evaluation
     * for example, for an AND predicate, if one child already get a false, the other child no longer need to be evaluated.
     *
     * If `pure` is set to true (which is the default) and all its input is a ConstantNodeFunction, this function
     * will attempt to preemptively call the function and store the result in a ConstantNodeFunction, which
     * may cut some evaluation time. This apply to other `fromFunction` variant as well.
     */
    inline fun <I: Any, reified R> fromFunctionMaker(
        crossinline func: (Array<NodeFunction<I, Any>>) -> NodeFunction<I, R>,
        args: List<NodeType>,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        noopInput: I? = null,
        pure: Boolean = true,
    ): TreeNodeFactory<NodeFunction<I, R>> {
        return FromFuncTreeNode.Factory({ input ->
            val inputVals = input.map {
                (it as NodeFunction<I, Any>)
            }.toTypedArray()

            if (pure && noopInput != null && inputVals.all { it is ConstantNodeFunction }) {
                ConstantNodeFunction(func(inputVals).call(noopInput))
            } else {
                func(inputVals)
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from a lambda that will be used in the generated function.
     */
    inline fun <I: Any, reified R, II> fromFunction(
        noinline func: (II) -> R,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        args: List<NodeType> = func.reflect()!!.parameters.map { NodeType.fromKotlinNodeType(it.type) },
        noopInput: I? = null,
        pure: Boolean = true,
    ): TreeNodeFactory<NodeFunction<I, R>> {
        return FromFuncTreeNode.Factory({ input ->
            if (pure && noopInput != null && input.all { it is ConstantNodeFunction<*> }) {
                val constantInput = (input[0] as NodeFunction<I, II>).call(noopInput)

                ConstantNodeFunction(func(constantInput))
            } else {
                val arg1 = input[0] as NodeFunction<I, II>

                NodeFunction { input: I ->
                    func(arg1.call(input))
                }
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from a lambda that will be used in the generated function.
     */
    inline fun <I: Any, reified R, I1, I2> fromFunction(
        noinline func: (I1, I2) -> R,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        args: List<NodeType> = func.reflect()!!.parameters.map { NodeType.fromKotlinNodeType(it.type) },
        noopInput: I? = null,
        pure: Boolean = true,
    ): TreeNodeFactory<NodeFunction<I, R>> {
        return FromFuncTreeNode.Factory({ input ->
            if (pure && noopInput != null && input.all { it is ConstantNodeFunction<*> }) {
                val constantInput = (input[0] as NodeFunction<I, I1>).call(noopInput)
                val constantInput2 = (input[1] as NodeFunction<I, I2>).call(noopInput)

                ConstantNodeFunction(func(constantInput, constantInput2))
            } else {
                val arg1 = input[0] as NodeFunction<I, I1>
                val arg2 = input[1] as NodeFunction<I, I2>

                NodeFunction { input: I ->
                    func(arg1.call(input), arg2.call(input))
                }
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from a lambda that will be used in the generated function.
     */
    inline fun <I: Any, reified R, I1, I2, I3> fromFunction(
        noinline func: (I1, I2, I3) -> R,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        args: List<NodeType> = func.reflect()!!.parameters.map { NodeType.fromKotlinNodeType(it.type) },
        noopInput: I? = null,
        pure: Boolean = true,
    ): TreeNodeFactory<NodeFunction<I, R>> {
        return FromFuncTreeNode.Factory({ input ->
            if (pure && noopInput != null && input.all { it is ConstantNodeFunction<*> }) {
                val constantInput = (input[0] as NodeFunction<I, I1>).call(noopInput)
                val constantInput2 = (input[1] as NodeFunction<I, I2>).call(noopInput)
                val constantInput3 = (input[2] as NodeFunction<I, I3>).call(noopInput)

                ConstantNodeFunction(func(constantInput, constantInput2, constantInput3))
            } else {
                val arg1 = input[0] as NodeFunction<I, I1>
                val arg2 = input[1] as NodeFunction<I, I2>
                val arg3 = input[1] as NodeFunction<I, I3>

                NodeFunction { input: I ->
                    func(arg1.call(input), arg2.call(input), arg3.call(input))
                }
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node factory from that uses the passed in function.
     * The resulting function have some call overhead.
     */
    inline fun <I: Any, reified R> fromKCallable(
        func: KCallable<R>,
        returnType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>()),
        args: List<NodeType> = func.parameters.map { NodeType.fromKotlinNodeType(it.type) },
        noopInput: I? = null,
        pure: Boolean = true,
    ): TreeNodeFactory<NodeFunction<I, R>> {
        return FromFuncTreeNode.Factory({ input ->
            if (pure && noopInput != null && input.all { it is ConstantNodeFunction<*> }) {
                val inputVals = input.map {
                    (it as ConstantNodeFunction<Any>).call(noopInput)
                }.toTypedArray()

                ConstantNodeFunction(func.call(*inputVals))
            } else {
                val inputValProducer = input.map {
                    (it as NodeFunction<I, Any>)
                }.toTypedArray()

                val inputVals = arrayOfNulls<Any>(inputValProducer.size)

                NodeFunction { input: I ->
                    var i = 0
                    while (i < args.size) {
                        inputVals[i] = inputValProducer[i].call(input)
                        i++
                    }
                    func.call(*inputVals)
                }
            }
        }, FunctionNodeType(returnType), args.map { FunctionNodeType(it) })
    }

    /**
     * Create a function tree node that extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R> fromArgs(argIdx: Int, nodeType: NodeType = NodeType.fromKotlinNodeType(typeOf<R>())): TreeNodeFactory<NodeFunction<CallCtx, R>> {
        return FromFuncTreeNode.Factory({
            NodeFunction { ctx ->
                ctx.args[argIdx] as R
            }
        }, FunctionNodeType(nodeType))
    }

    /**
     * Create a tree node factory that creates a function tree node that always return the constant specified
     */
    inline fun <reified R> fromConstant(constant: R): TreeNodeFactory<NodeFunction<Any, R>> {
        return fromFunctionMaker({ ConstantNodeFunction(constant) }, listOf())
    }

    /**
     * Create a function tree node that finally extract the input of the whole tree from the CallCtx
     * This is basically the tree terminal that gets input.
     */
    inline fun <reified R: Any> fromGenerator(noinline generator: () -> R): TreeNodeFactory<NodeFunction<Any, R>> {
        return GeneratorTreeNode.Factory(generator, returnType = functionalNodeTypeFromKType(typeOf<R>()))
    }

    /**
     * Create a tree node that will create a function that always return a constant. Mainly used in testing.
     */
    fun <R> createConstantTreeNode(constant: R, type: NodeType = FunctionNodeType(KotlinNodeType(constant!!::class.createType())))
            : TreeNode<NodeFunction<Any, R>> {
        return FromFuncTreeNode.Factory({
            NodeFunction<Any, R> { constant }
        }, type).createNode(listOf())
    }
}
