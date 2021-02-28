package com.asdacap.ea4k

import org.junit.jupiter.api.Test
import kotlin.random.Random.Default.nextFloat

internal class AlgorithmsKtTest {

    val sphereBenchmark = FunctionalToolbox<List<Float>, Float>(
        evaluateFn = {
            it.map { it*it }.sum()
        },
        selectFn = { list, k ->
            selTournament(list, k, 10, compareBy { it.fitness!! * -1 })
        },
        mateFn = { id1, id2 ->
            cxUniform(id1, id2, 0.5)
        },
        mutateFn = {
            it.map {
                if (nextFloat() < 0.5f) {
                    nextFloat()
                } else {
                    it
                }
            }
        }
    )

    /**
     * Simple test for simple case
     */
    @Test
    fun testEaSimple() {
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
    }

    /**
     * Simple test for simple case
     */
    @Test
    fun testEeMuPlusLambda() {
        val startingPop = (1..1000).map {
            (1..10).map {
                nextFloat()
            }
        }

        val result = eaMuPlusLambda(
            startingPop.map { IndividualWithFitness(it, null) },
            sphereBenchmark,
            mu = 100,
            lambda_ = 200,
            cxpb = 0.3f,
            mutpb = 0.2f,
            ngen = 100
        )

        assert(result.sortedBy { it.fitness }.first().fitness!! < 0.1f)
    }

    /**
     * Simple test for simple case
     */
    @Test
    fun testEaMuCommaLambda() {
        val startingPop = (1..1000).map {
            (1..10).map {
                nextFloat()
            }
        }

        val result = eaMuCommaLambda(
            startingPop.map { IndividualWithFitness(it, null) },
            sphereBenchmark,
            mu = 100,
            lambda_ = 200,
            cxpb = 0.3f,
            mutpb = 0.2f,
            ngen = 100
        )

        assert(result.sortedBy { it.fitness }.first().fitness!! < 0.1f)
    }
}