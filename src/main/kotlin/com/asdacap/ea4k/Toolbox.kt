package com.asdacap.ea4k

import java.util.stream.Collectors

/**
 * Basically its a collection of configuration that algorithms typically use.
 * Both individual and fitness is expected to be immutable.
 * Only `evaluate` does not have default implementation.
 *
 * I for Individual
 * F for Fitness
 */
interface Toolbox<I, F> {
    /**
     * Evaluate the individual. Return null if invalid.
     * What happen to invalid individual depends on algorithms
     */
    fun evaluate(individual: I): F?

    /**
     * Evaluate multiple individuals. Override if it can be optimized
     * Default implementation uses `map` and delegate to per-individual evaluate.
     */
    fun evaluate(individuals: List<I>): List<F?> {
        return map(::evaluate, individuals)
    }

    /**
     * Select a subset of the individuals. Default implementation just take the first k individuals.
     */
    fun select(list: List<IndividualWithFitness<I, F>>, k: Int): List<IndividualWithFitness<I, F>> {
        return list.take(k)
    }

    /**
     * Mate the two individuals. Default implementation does not do any mating and return the same individuals.
     */
    fun mate(individual: I, individual2: I): Pair<I, I> = Pair(individual, individual2)

    /**
     * Mate the two individuals. Default implementation delegates to `mate`
     * Algorithms should use this method as it attempts to retain evaluated fitness.
     */
    fun mateWithFitness(individual: IndividualWithFitness<I, F>, individual2: IndividualWithFitness<I, F>): Pair<IndividualWithFitness<I, F>, IndividualWithFitness<I, F>> {
        val (ni1, ni2) = mate(individual.individual, individual2.individual)

        return (if (ni1 === individual.individual) { individual } else { IndividualWithFitness(ni1, null as F) }) to
                (if (ni2 === individual2.individual) { individual2 } else { IndividualWithFitness(ni2, null as F) })
    }

    /**
     * Mutate the individual. Default implementation does nothing and return the same individual.
     */
    fun mutate(individual: I): I = individual

    /**
     * Mutate the individual. Default implementation delegate to `mutate`.
     * Algorithms should use this method as it attempts to retain evaluated fitness.
     */
    fun mutateWithFitness(individual: IndividualWithFitness<I, F>): IndividualWithFitness<I, F> {
        val mutated = mutate(individual.individual)
        if (mutated === individual.individual) {
            return individual
        }
        return IndividualWithFitness(mutated, null)
    }

    /**
     * A map executor. Used to provide multithreading. Override to disable multithreading or use a custom thread pool.
     * Algorithms should use this where possible.
     */
    fun <T, R> map(mapper: (T) -> R, items: List<T>): List<R> {
        return items
            .parallelStream()
            .map(mapper)
            .collect(Collectors.toList())
    }

    /**
     * Callback that is called on each generation.
     */
    fun onGeneration(population: List<IndividualWithFitness<I, F>>) {
    }
}