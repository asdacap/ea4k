package com.asdacap.ea4k.gp

import com.asdacap.ea4k.Utils
import kotlin.random.Random

object Generator {

    /**
     * Generate a Tree as a list of list. The tree is build from the root to the leaves, and it stop growing when the
     * condition is fulfilled.
     * @param pset Primitive set from which primitives are selected.
     * @param min_ Minimum height of the produced trees.
     * @param max_ Maximum Height of the produced trees.
     * @param condition The condition is a function that takes two arguments the height of the tree to build and the current
     *  depth in the tree. If it return true, it will attempt to get only terminals.
     * @param type_: The type that should return by the tree when evaluated.
     * @return A grown tree with leaves at possibly different depths depending on the condition function.
     */
    fun <R> generate(pset: PSet<R>, min: Int, max: Int, condition: (Int, Int) -> Boolean, type: NodeType): TreeNode<*> {
        val height = Random.nextInt(min, max)

        var recurGen: ((Int, NodeType) -> TreeNode<*>)? = null
        recurGen = { depth, ret ->
            if (condition(height, depth)) {
                val terminalOpts = pset.getTerminalAssignableTo(ret)
                if (terminalOpts.isEmpty()) {
                    throw Exception("The ea4k.gp.generate function tried to add " +
                            "a terminal of type $ret, but there is " +
                            "none available.")
                }
                val pickedTerminal = Utils.randomChoice(terminalOpts)
                pickedTerminal.createNode(listOf())
            } else {
                val primitiveOpts = pset.getPrimitiveAssignableTo(ret)
                if (primitiveOpts.isEmpty()) {
                    throw Exception("The ea4k.gp.generate function tried to add " +
                            "a primitive of type '$ret', but there is " +
                            "none available.".format(ret))
                }
                val primitive = Utils.randomChoice(primitiveOpts)
                primitive.createNode(primitive.args.map { recurGen!!.invoke(depth+1, it) }.toList())
            }
        }

        return recurGen(0, type)
    }

    /**
     *
     * Generate an expression where each leaf has the same depth
     * between *min* and *max*.
     * @param pset Primitive set from which primitives are selected.
     * @param min_ Minimum height of the produced trees.
     * @param max_ Maximum Height of the produced trees.
     * @param type_ The type that the tree should return when evaluated
     * @return A full tree with all leaves at the same depth.
     */
    fun <R> genFull(pset: PSet<R>, min: Int, max: Int, type: NodeType): TreeNode<*> {
        return generate(pset, min, max, {h, d -> h == d}, type)
    }

    /**
     * Generate an expression where each leaf might have a different depth
     * between *min* and *max*.
     * @param pset Primitive set from which primitives are selected.
     * @param min_ Minimum height of the produced trees.
     * @param max_ Maximum Height of the produced trees.
     * @param type_ The type that the tree should return when evaluated
     * @return A grown tree with leaves at possibly different depths.
     */
    fun <R> genGrow(pset: PSet<R>, min: Int, max: Int, type: NodeType): TreeNode<*> {
        val cond: (Int, Int) -> Boolean = { h, d ->
            d == h || (d >= min && Random.nextDouble() < pset.terminalRatio)
        }
        return generate(pset, min, max, cond, type)
    }

    /**
     * Generate an expression with a PrimitiveSet *pset*.
     * Half the time, the expression is generated with :func:`~deap.ea4k.gp.genGrow`,
     * the other half, the expression is generated with :func:`~deap.ea4k.gp.genFull`.
     * @param pset Primitive set from which primitives are selected.
     * @param min_ Minimum height of the produced trees.
     * @param max_ Maximum Height of the produced trees.
     * @param type_ The type that the tree should return when evaluated
     * @returns: Either, a full or a grown tree.
     */
    fun <R> genHalfAndHalf(pset: PSet<R>, min: Int, max: Int, type: NodeType): TreeNode<*> {
        if (Random.nextDouble() < 0.5) {
            return genGrow(pset, min, max, type)
        }
        return genFull(pset, min, max, type)
    }

}