package com.asdacap.ea4k.gp

import com.asdacap.ea4k.Utils
import kotlin.random.Random

object Generator {
    fun <R> generate(pset: PSet<R>, min: Int, max: Int, condition: (Int, Int) -> Boolean, type: NodeType): TreeNode<*> {
        /*
        """Generate a Tree as a list of list. The tree is build
        from the root to the leaves, and it stop growing when the
        condition is fulfilled.
        :param pset: Primitive set from which primitives are selected.
        :param min_: Minimum height of the produced trees.
        :param max_: Maximum Height of the produced trees.
        :param condition: The condition is a function that takes two arguments,
                          the height of the tree to build and the current
                          depth in the tree.
        :param type_: The type that should return the tree when called, when
                      :obj:`None` (default) the type of :pset: (pset.ret)
                      is assumed.
        :returns: A grown tree with leaves at possibly different depths
                  depending on the condition function.
        """
         */

        val height = Random.nextInt(min, max)

        var recurGen: ((Int, NodeType) -> TreeNode<*>)? = null
        recurGen = { depth, ret ->
            if (condition(height, depth)) {
                val terminalOpts = pset.getTerminalAssignableTo(ret)
                if (terminalOpts == null || terminalOpts.size == 0) {
                    throw Exception("The ea4k.gp.generate function tried to add " +
                            "a terminal of type $ret, but there is " +
                            "none available.")
                }
                val pickedTerminal = Utils.randomChoice(terminalOpts)
                pickedTerminal.createNode(listOf())
            } else {
                val primitiveOpts = pset.getPrimitiveAssignableTo(ret)
                if (primitiveOpts == null || primitiveOpts.size == 0) {
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

    fun <R> genFull(pset: PSet<R>, min: Int, max: Int, type: NodeType): TreeNode<*> {
        """Generate an expression where each leaf has the same depth
    between *min* and *max*.
    :param pset: Primitive set from which primitives are selected.
    :param min_: Minimum height of the produced trees.
    :param max_: Maximum Height of the produced trees.
    :param type_: The type that should return the tree when called, when
                  :obj:`None` (default) the type of :pset: (pset.ret)
                  is assumed.
    :returns: A full tree with all leaves at the same depth.
    """

        return generate(pset, min, max, {h, d -> h == d}, type)
    }

    fun <R> genGrow(pset: PSet<R>, min: Int, max: Int, type: NodeType): TreeNode<*> {
        """Generate an expression where each leaf might have a different depth
    between *min* and *max*.
    :param pset: Primitive set from which primitives are selected.
    :param min_: Minimum height of the produced trees.
    :param max_: Maximum Height of the produced trees.
    :param type_: The type that should return the tree when called, when
                  :obj:`None` (default) the type of :pset: (pset.ret)
                  is assumed.
    :returns: A grown tree with leaves at possibly different depths.
    """

        val cond: (Int, Int) -> Boolean = { h, d ->
            d == h || (d >= min && Random.nextFloat() < pset.terminalRatio)
        }
        return generate(pset, min, max, cond, type)
    }

    /*
    def genHalfAndHalf(pset, min_, max_, type_=None):
        """Generate an expression with a PrimitiveSet *pset*.
        Half the time, the expression is generated with :func:`~deap.ea4k.gp.genGrow`,
        the other half, the expression is generated with :func:`~deap.ea4k.gp.genFull`.
        :param pset: Primitive set from which primitives are selected.
        :param min_: Minimum height of the produced trees.
        :param max_: Maximum Height of the produced trees.
        :param type_: The type that should return the tree when called, when
                      :obj:`None` (default) the type of :pset: (pset.ret)
                      is assumed.
        :returns: Either, a full or a grown tree.
        """
        method = random.choice((genGrow, genFull))
        return method(pset, min_, max_, type_)
     */
    fun <R> genHalfAndHalf(pset: PSet<R>, min: Int, max: Int, type: NodeType): TreeNode<*> {
        if (Random.nextFloat() < 0.5) {
            return genGrow(pset, min, max, type)
        }
        return genFull(pset, min, max, type)
    }

}