package com.asdacap.ea4k.gp

import com.asdacap.ea4k.Utils.randomChoice
import kotlin.reflect.KType

object Mutator {
    fun <R> cxOnePoint(tr1: BaseTreeNode<R>, tr2: BaseTreeNode<R>): Pair<BaseTreeNode<R>, BaseTreeNode<R>> {
        """Randomly select crossover point in each individual and exchange each
    subtree with the point as root between each individual.
    :param ind1: First tree participating in the crossover.
    :param ind2: Second tree participating in the crossover.
    :returns: A tuple of two trees.
    """

        if (tr1.size == 1 || tr2.size == 1) {
            // No crossover on single node tree
            return Pair(tr1, tr2)
        }

        val tr1TypeMap: MutableMap<KType, MutableList<BaseTreeNode<*>>> = mutableMapOf()
        val tr2TypeMap: MutableMap<KType, MutableList<BaseTreeNode<*>>> = mutableMapOf()
        val tr1Types: MutableSet<KType> = mutableSetOf();
        val tr2Types: MutableSet<KType> = mutableSetOf();

        tr1.iterateAll().forEach {
            tr1TypeMap.getOrPut(it.returnType, { mutableListOf() }).add(it)
            tr1Types.add(it.returnType)
        }
        tr2.iterateAll().forEach {
            tr2TypeMap.getOrPut(it.returnType, { mutableListOf() }).add(it)
            tr2Types.add(it.returnType)
        }

        val commonTypes = tr1Types.intersect(tr2Types)

        if (commonTypes.size > 0) {
            val chosenType = randomChoice(commonTypes.toList());

            val tr1Idx = randomChoice(tr1TypeMap[chosenType]!!)
            val tr2Idx = randomChoice(tr2TypeMap[chosenType]!!)

            // This does mean that the top level node can never be swapped
            return Pair(
                tr1.replaceChild(tr1Idx, tr2Idx) as BaseTreeNode<R>,
                tr2.replaceChild(tr2Idx, tr1Idx) as BaseTreeNode<R>
            )
        }

        return Pair(tr1, tr2)
    }

/*
######################################
# GP Mutations                       #
######################################
def mutUniform(individual, expr, pset):
    """Randomly select a point in the tree *individual*, then replace the
    subtree at that point as a root by the expression generated using method
    :func:`expr`.
    :param individual: The tree to be mutated.
    :param expr: A function object that can generate an expression when
                 called.
    :returns: A tuple of one tree.
    """
    index = random.randrange(len(individual))
    slice_ = individual.searchSubtree(index)
    type_ = individual[index].ret
    individual[slice_] = expr(pset=pset, type_=type_)
    return individual,
 */

    fun mutUniform(treeNode: BaseTreeNode<*>, expr: (KType) -> BaseTreeNode<*>): BaseTreeNode<*>? {
        val allSub = treeNode.iterateAll()
        if (allSub.size == 0) {
            return null
        }
        val selected = randomChoice(allSub);
        val returnType = selected.returnType
        val generated = expr(returnType)
        if (generated.returnType != returnType) {
            throw Exception("unexpected return type")
        }
        return treeNode.replaceChild(selected, generated)
    }




}
