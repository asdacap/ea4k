package com.asdacap.ea4k.gp

import com.asdacap.ea4k.*
import com.asdacap.ea4k.Utils.mateCutoff
import com.asdacap.ea4k.Utils.mutateCutoff
import com.asdacap.ea4k.gp.Mutator.cxOnePoint
import com.asdacap.ea4k.gp.Mutator.mutRecreateState
import com.asdacap.ea4k.gp.Mutator.mutUniform
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromConstant
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromArgs
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromFunction
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromGenerator
import com.asdacap.ea4k.gp.functional.CallCtx
import com.asdacap.ea4k.gp.functional.NodeFunction
import com.asdacap.ea4k.gp.functional.FunctionNodeType
import com.asdacap.ea4k.gp.functional.FunctionNodeType.Companion.functionalNodeTypeFromKType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.random.Random
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextFloat
import kotlin.reflect.typeOf

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