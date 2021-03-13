package com.asdacap.ea4k

import java.lang.RuntimeException

fun <I, F> toolboxWithEvaluate(evaluateFn: (I) -> F?): Toolbox<I, F> {
    return object: Toolbox<I, F> {
        override fun evaluate(individual: I): F? {
            return evaluateFn(individual)
        }
    }
}

fun <I, F> toolboxWithBatchEvaluate(evaluateFn: (List<I>) -> List<F?>): Toolbox<I, F> {
    return object: Toolbox<I, F> {
        override fun evaluate(individual: I): F? {
            throw RuntimeException("Not implemented")
        }

        override fun evaluate(individuals: List<I>): List<F?> {
            return evaluateFn(individuals)
        }
    }
}

fun <I, F> Toolbox<I, F>.withSelect(selectFn: (List<IndividualWithFitness<I, F>>, Int) -> List<IndividualWithFitness<I, F>>): Toolbox<I, F> {
    return object: Toolbox<I, F> by this {
        override fun select(list: List<IndividualWithFitness<I, F>>, k: Int): List<IndividualWithFitness<I, F>> {
            return selectFn(list, k)
        }
    }
}

fun <I, F> Toolbox<I, F>.withMate(mateFn: (I, I) -> Pair<I, I>): Toolbox<I, F> {
    return object: Toolbox<I, F> by this {
        override fun mate(individual: I, individual2: I): Pair<I, I> {
            return mateFn(individual, individual2)
        }

        // Because of delegation, this is needed
        override fun mateWithFitness(
            individual: IndividualWithFitness<I, F>,
            individual2: IndividualWithFitness<I, F>
        ): Pair<IndividualWithFitness<I, F>, IndividualWithFitness<I, F>> {
            val (ni1, ni2) = mate(individual.individual, individual2.individual)

            return (if (ni1 === individual.individual) { individual } else { IndividualWithFitness(ni1, null as F) }) to
                    (if (ni2 === individual2.individual) { individual2 } else { IndividualWithFitness(ni2, null as F) })
        }
    }
}

fun <I, F> Toolbox<I, F>.withMateWithFitness(
    mateFn: (IndividualWithFitness<I, F>, IndividualWithFitness<I, F>) -> Pair<IndividualWithFitness<I, F>, IndividualWithFitness<I, F>>
): Toolbox<I, F> {
    return object: Toolbox<I, F> by this {
        override fun mateWithFitness(
            individual: IndividualWithFitness<I, F>,
            individual2: IndividualWithFitness<I, F>
        ): Pair<IndividualWithFitness<I, F>, IndividualWithFitness<I, F>> {
            return mateFn(individual, individual2)
        }
    }
}

fun <I, F> Toolbox<I, F>.withSingleThreadMapper(): Toolbox<I, F> {
    return object: Toolbox<I, F> by this {
        override fun <T, R> map(mapper: (T) -> R, items: List<T>): List<R> {
            return items.map(mapper)
        }
    }
}

fun <I, F> Toolbox<I, F>.withMutate(mutateFn: (I) -> I): Toolbox<I, F> {
    return object: Toolbox<I, F> by this {
        override fun mutate(individual: I): I {
            return mutateFn(individual)
        }

        // Because of delegation, this is needed
        override fun mutateWithFitness(individual: IndividualWithFitness<I, F>): IndividualWithFitness<I, F> {
            val mutated = mutate(individual.individual)
            if (mutated === individual.individual) {
                return individual
            }
            return IndividualWithFitness(mutated, null)
        }
    }
}

fun <I, F> Toolbox<I, F>.withMutateWithFitness(
    mutateFn: (IndividualWithFitness<I, F>) -> IndividualWithFitness<I, F>
): Toolbox<I, F> {
    return object: Toolbox<I, F> by this {
        override fun mutate(individual: I): I {
            throw RuntimeException("Not implemented")
        }

        override fun mutateWithFitness(individual: IndividualWithFitness<I, F>): IndividualWithFitness<I, F> {
            return mutateFn(individual)
        }
    }
}

fun <I, F> Toolbox<I, F>.withOnGeneration(onGenerationFn: (List<IndividualWithFitness<I, F>>) -> Unit): Toolbox<I, F> {
    return object: Toolbox<I, F> by this {
        override fun onGeneration(population: List<IndividualWithFitness<I, F>>) {
            return onGenerationFn(population)
        }
    }
}

