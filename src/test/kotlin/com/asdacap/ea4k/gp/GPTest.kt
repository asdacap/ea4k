package com.asdacap.ea4k.gp

import com.asdacap.ea4k.*
import com.asdacap.ea4k.gp.Mutator.cxOnePoint
import com.asdacap.ea4k.gp.Mutator.mutUniform
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromConstant
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromArgs
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromFunction
import com.asdacap.ea4k.gp.functional.FunctionTreeNodeConstructors.fromGenerator
import com.asdacap.ea4k.gp.functional.CallCtx
import com.asdacap.ea4k.gp.functional.NodeFunction
import com.asdacap.ea4k.gp.functional.FunctionNodeType
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.random.Random
import kotlin.random.Random.Default.nextFloat
import kotlin.reflect.typeOf

class GPTest {

    val pset = PSet<(Float) -> Float>(KotlinNodeType(typeOf<(Float) -> Float>()))

    fun multiply(n1: Float, n2: Float): Float = n1 * n2
    fun add(n1: Float, n2: Float): Float = n1 + n2
    fun subtract(n1: Float, n2: Float): Float = n1 - n2

    init {
        pset.addTreeNodeFactory("ARG0", fromArgs<Float>(0))
        pset.addTreeNodeFactory("Mul", fromFunction(::multiply))
        pset.addTreeNodeFactory("Add", fromFunction(::add))
        pset.addTreeNodeFactory("Sub", fromFunction(::subtract))
        pset.addTreeNodeFactory("Constant1", fromConstant(1.0f))
        pset.addTreeNodeFactory("ConstantNeg99", fromConstant(-99.0f))
        pset.addTreeNodeFactory("Random", fromGenerator { nextFloat() })
    }

    fun treeGenerator(nodeType: NodeType = FunctionNodeType(KotlinNodeType(typeOf<Float>()))): TreeNode<*> {
        return Generator.genHalfAndHalf(pset, 0, 2, nodeType)
    }

    val MAX_SIZE = 100
    val experiment = toolboxWithEvaluate<TreeNode<NodeFunction<Float>>, Float>
    { individual ->
        val rand = Random(0)
        (0..5).map {
            val inp = rand.nextFloat() * 100
            val answer = inp * (inp + 1) * (inp - 99) // The equation to guess
            val test = individual.evaluate().call(CallCtx(arrayOf(inp)))
            val diff = answer - test
            diff * diff
        }.sum()
    }.withSelect { list, k ->
        Selection.selTournament(list, k, 5, compareBy { it.fitness!! * -1 })
    }.withMate { id1, id2 ->
        var (c1, c2) = cxOnePoint(id1, id2)
        c1 = if (c1.size > MAX_SIZE) {
            id1
        } else {
            c1
        }
        c2 = if (c2.size > MAX_SIZE) {
            id2
        } else {
            c2
        }
        c1 to c2
    }.withMutate {
        var result = mutUniform(it, ::treeGenerator) as TreeNode<NodeFunction<Float>>
        if (result.size > MAX_SIZE) {
            it
        } else {
            result
        }
    }.withOnGeneration {
        val minValue = it.sortedBy { it.fitness!! }.first()
        System.out.println(minValue.fitness)
        val asJson = pset.serialize(minValue.individual)
        File("ind.json").writeText(asJson.toPrettyString())
    }

    /**
     * Simple test for simple case
     */
    @Test
    fun testBasic() {

        val populationCount = 1000
        val result = Algorithms.eaMuCommaLambda(
            (1..populationCount).map { IndividualWithFitness(treeGenerator() as TreeNode<NodeFunction<Float>>, null) },
            experiment,
            mu = populationCount,
            lambda_ = populationCount * 2,
            cxpb = 0.8f,
            mutpb = 0.1f,
            ngen = 100
        )

        assert(result.sortedBy { it.fitness }.first().fitness!! < 0.1f)
    }
}