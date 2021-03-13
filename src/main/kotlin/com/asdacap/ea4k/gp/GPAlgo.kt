package com.asdacap.ea4k.gp

import com.asdacap.ea4k.Utils.randomChoice

object Mutator {
    /**
     * Randomly select crossover point in each individual and exchange each
     * subtree with the point as root between each individual.
     * @param ind1 First tree participating in the crossover.
     * @param ind2 Second tree participating in the crossover.
     * @return A tuple of two trees.
     */
    fun <R> cxOnePoint(tr1: TreeNode<R>, tr2: TreeNode<R>): Pair<TreeNode<R>, TreeNode<R>> {
        if (tr1.size == 1 || tr2.size == 1) {
            // No crossover on single node tree
            return Pair(tr1, tr2)
        }

        val tr1TypeMap: MutableMap<NodeType, MutableList<TreeNode<*>>> = mutableMapOf()
        val tr2TypeMap: MutableMap<NodeType, MutableList<TreeNode<*>>> = mutableMapOf()
        val tr1Types: MutableSet<NodeType> = mutableSetOf();
        val tr2Types: MutableSet<NodeType> = mutableSetOf();

        tr1.iterateAll().forEach {
            tr1TypeMap.getOrPut(it.factory.returnType, { mutableListOf() }).add(it)
            tr1Types.add(it.factory.returnType)
        }
        tr2.iterateAll().forEach {
            tr2TypeMap.getOrPut(it.factory.returnType, { mutableListOf() }).add(it)
            tr2Types.add(it.factory.returnType)
        }

        val commonTypes = tr1Types.intersect(tr2Types)

        if (commonTypes.size > 0) {
            val chosenType = randomChoice(commonTypes.toList());

            val tr1Child = randomChoice(tr1TypeMap[chosenType]!!)
            val tr2Child = randomChoice(tr2TypeMap[chosenType]!!)

            return Pair(
                tr1.replaceChild(tr1Child, tr2Child),
                tr2.replaceChild(tr2Child, tr1Child)
            )
        }

        return Pair(tr1, tr2)
    }

    /**
     * Randomly select a point in the tree *individual*, then replace the
     * subtree at that point as a root by the expression generated using method *expr*
     * @param individual The tree to be mutated.
     * @param expr A function object that can generate an subtree when called.
     * @return A tree.
     */
    fun <R> mutUniform(treeNode: TreeNode<R>, expr: (NodeType) -> TreeNode<*>): TreeNode<R> {
        val allSub = treeNode.iterateAll()
        val selected = randomChoice(allSub);
        val returnType = selected.factory.returnType
        val generated = expr(returnType)
        return treeNode.replaceChild(selected, generated)
    }

    /**
     * Randomly select a stateful tree node in *individual*, then replace the
     * subtree at that point with a new stateful tree node.
     * @param individual The tree to be mutated.
     * @return A tree.
     */
    fun <R> mutRecreateState(treeNode: TreeNode<R>): TreeNode<R> {
        val allSub = treeNode.iterateAll()
            .filter { it.isStateful }
        if (allSub.isEmpty()) {
            return treeNode
        }

        val selected = randomChoice(allSub);
        val regenerated = selected.factory.createNode(selected.children)
        return treeNode.replaceChild(selected, regenerated)
    }
}
