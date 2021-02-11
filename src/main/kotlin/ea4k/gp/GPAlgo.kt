import ea4k.gp.*
import ea4k.gp.Utils.randomChoice
import ea4k.gp.Utils.randomChoiceLastBiased
import kotlin.random.Random
import kotlin.reflect.KType

fun <R> generate(pset: PSet<R>, min: Int, max: Int, condition: (Int, Int) -> Boolean, type: KType): BaseTreeNode<*> {
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

    var recurGen: ((Int, KType) -> BaseTreeNode<*>)? = null
    recurGen = { depth, ret ->
        if (condition(height, depth)) {
            val terminalOpts = pset.terminals[ret]
            if (terminalOpts == null || terminalOpts.size == 0) {
                throw Exception("The ea4k.gp.generate function tried to add " +
                        "a terminal of type $ret, but there is " +
                        "none available.")
            }
            val pickedTerminal = randomChoice(terminalOpts)
            pickedTerminal.createNode(listOf())
        } else {
            val primitiveOpts = pset.primitives[ret]
            if (primitiveOpts == null || primitiveOpts.size == 0) {
                throw Exception("The ea4k.gp.generate function tried to add " +
                        "a primitive of type '$ret', but there is " +
                        "none available.".format(ret))
            }
            val primitive = randomChoice(primitiveOpts)
            primitive.createNode(primitive.args.map { recurGen!!.invoke(depth+1, it) }.toList())
        }
    }

    return recurGen(0, type)
}

fun <R> genFull(pset: PSet<R>, min: Int, max: Int, type: KType): BaseTreeNode<*> {
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

fun <R> genGrow(pset: PSet<R>, min: Int, max: Int, type: KType): BaseTreeNode<*> {
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

fun getReturnType(pair: Pair<BaseTreeNode<*>, Int>): KType {
    return pair.first.children[pair.second].returnType;
}

fun swapTree(pair: Pair<BaseTreeNode<*>, Int>, pair2: Pair<BaseTreeNode<*>, Int>) {
    val node1 = pair.first.children[pair.second]
    val node2 = pair2.first.children[pair2.second]
    pair.first.children[pair.second] = node2
    pair2.first.children[pair2.second] = node1
}

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

    val tr1TypeMap: MutableMap<KType, MutableList<Pair<BaseTreeNode<*>, Int>> > = mutableMapOf()
    val tr2TypeMap: MutableMap<KType, MutableList<Pair<BaseTreeNode<*>, Int>> > = mutableMapOf()
    val tr1Types: MutableSet<KType> = mutableSetOf();
    val tr2Types: MutableSet<KType> = mutableSetOf();

    tr1.iterateAllWithParentAndIndex().forEach {
        tr1TypeMap.getOrPut(getReturnType(it), { mutableListOf() }).add(it)
        tr1Types.add(getReturnType(it))
    }
    tr2.iterateAllWithParentAndIndex().forEach {
        tr2TypeMap.getOrPut(getReturnType(it), { mutableListOf() }).add(it)
        tr2Types.add(getReturnType(it))
    }

    val commonTypes = tr1Types.intersect(tr2Types)

    if (commonTypes.size > 0) {
        val chosenType = randomChoice(commonTypes.toList());

        val tr1Idx = randomChoiceLastBiased(tr1TypeMap[chosenType]!!)
        val tr2Idx = randomChoiceLastBiased(tr2TypeMap[chosenType]!!)

        // This does mean that the top level node can never be swapped
        swapTree(tr1Idx, tr2Idx)
    }

    return Pair(tr1, tr2)
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
fun <R> genHalfAndHalf(pset: PSet<R>, min: Int, max: Int, type: KType): BaseTreeNode<*> {
    if (Random.nextFloat() < 0.5) {
        return genGrow(pset, min, max, type)
    }
    return genFull(pset, min, max, type)
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
    val allSub = treeNode.iterateAllWithParentAndIndex()
    if (allSub.size == 0) {
        return null
    }
    val selected = randomChoiceLastBiased(allSub);
    val returnType = getReturnType(selected)
    val generated = expr(returnType)
    if (generated.returnType != returnType) {
        throw Exception("unexpected return type")
    }
    selected.first.children[selected.second] = generated
    return treeNode
}




