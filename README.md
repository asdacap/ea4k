Evolutionary Algorithms for Kotlin
==================================

A poor man's (very) partial reimplementation of [DEAP](https://deap.readthedocs.io/en/master/) library, but in Kotlin... 
and not distributed.

What has been implemented
-------------------------

- Most of the main evolutionary algorithms.
- Rewrote the tree-based structure for Kotlin use.
- Created a functional tree factories which generate tree that evaluate to function for reflection-free evaluation.

What is different
-----------------

- Things are type safe. Individual and Fitness are templated.
- Because we can't dynamically add property to individuals (not easily at least), fitness is not a property of 
  individual, both are wrapped under `IndividualWithFitness`.
- Because objects are very hard to clone properly, every individual
  is assumed to be immutable.
  - This also means functions that mutates input in DEAP is converted
    to function that return an output.
    
Example
-------

### Basic list individuals
```kotlin
val sphereBenchmark = toolboxWithEvaluate<List<Float>, Float> {
  it.map { it*it }.sum()
}.withSelect { list, k ->
  selTournament(list, k, 10, compareBy { it.fitness!! * -1 })
}.withMate { id1, id2 ->
  cxUniform(id1, id2, 0.5)
}.withMutate {
  it.map {
    if (nextFloat() < 0.5f) {
      nextFloat()
    } else {
      it
    }
  }
}

val startingPop = (1..1000).map {
  (1..10).map {
    nextFloat()
  }
}

val result = eaSimple(
  startingPop.map { IndividualWithFitness(it, null) },
  sphereBenchmark,
  cxpb = 0.3f,
  mutpb = 0.2f,
  ngen = 100
)

assert(result.sortedBy { it.fitness }.first().fitness!! < 0.1f)
```

### Guessing an equation
```kotlin
class GPTest {
  val MAX_SIZE = 100

  val pset = PSet<NodeFunction<CallCtx, Float>>(functionalNodeTypeFromKType(typeOf<Float>()))

  fun multiply(n1: Float, n2: Float): Float = n1 * n2
  fun add(n1: Float, n2: Float): Float = n1 + n2
  fun subtract(n1: Float, n2: Float): Float = n1 - n2

  init {
    pset.addTreeNodeFactory("ARG0", fromArgs<Float>(0))
    pset.addTreeNodeFactory("Mul", fromFunction<CallCtx, Float, Float, Float>(::multiply))
    pset.addTreeNodeFactory("Add", fromFunction<CallCtx, Float, Float, Float>(::add))
    pset.addTreeNodeFactory("Sub", fromFunction<CallCtx, Float, Float, Float>(::subtract))
    pset.addTreeNodeFactory("Constant1", fromConstant(1.0f))
    pset.addTreeNodeFactory("ConstantNeg99", fromConstant(-99.0f))
    pset.addTreeNodeFactory("Random", fromGenerator { nextFloat() })
  }

  fun treeGenerator(nodeType: NodeType = FunctionNodeType(KotlinNodeType(typeOf<Float>()))): TreeNode<*> {
    return Generator.genHalfAndHalf(pset, 0, 2, nodeType)
  }

  fun targetEquation(inp: Float) = inp * (inp + 1) * (inp - 99)
  fun nodeFilter(ind: TreeNode<NodeFunction<CallCtx, Float>>) = ind.size < MAX_SIZE

  fun evaluate(individual: TreeNode<NodeFunction<CallCtx, Float>>): Float {
    val rand = Random(0)
    val func = individual.evaluate()
    return (0..5).map {
      val inp = rand.nextFloat() * 100
      val answer = targetEquation(inp) // The equation to guess
      val test = func.call(CallCtx(inp))
      val diff = answer - test
      diff * diff
    }.sum()
  }

  @Test
  fun testBasic() {
    (0..5).forEach {
      if (runExperiment()) {
        return
      }
    }
    fail("Unable to converge after 5 tries")
  }

  fun runExperiment(): Boolean {
    val experiment = toolboxWithEvaluate<TreeNode<NodeFunction<CallCtx, Float>>, Float>
    { individual ->
      evaluate(individual)
    }.withSelect { list, k ->
      Selection.selTournament(list, k, 5, compareBy { it.fitness!! * -1 })
    }.withMate(
      mateCutoff(::cxOnePoint, ::nodeFilter)
    ).withMutate {
      mutateCutoff({
        if (nextDouble() < 0.5) {
          mutUniform(it, ::treeGenerator)
        } else {
          mutRecreateState(it)
        }
      }, ::nodeFilter) (it)
    }.withOnGeneration {
      /*
      val minValue = it.sortedBy { it.fitness!! }.first()
      System.out.println(minValue.fitness)
      val asJson = pset.serialize(minValue.individual)
      File("ind.json").writeText(asJson.toPrettyString())
       */
    }

    val populationCount = 1000
    val result = Algorithms.eaMuCommaLambda(
      @Suppress("UNCHECKED_CAST")
      (1..populationCount).map { IndividualWithFitness(treeGenerator() as TreeNode<NodeFunction<CallCtx, Float>>, null) },
      experiment,
      mu = populationCount,
      lambda_ = populationCount * 5,
      cxpb = 0.8,
      mutpb = 0.1,
      ngen = 50
    )

    return result.sortedBy { it.fitness }.first().fitness!! < 0.1f
  }
}
```

License
-------

MIT