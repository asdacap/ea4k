package ea4k

import java.util.stream.Collectors

/**
 * Basically its a collection of configuration that algorithms typically use
 * Both individual and fitness is expected to be immutable
 *
 * I for Individual
 * F for Fitness
 */
interface Toolbox<I, F> {
    // Evaluate the individual. Return null if invalid.
    // What happen to invalid individual depends on the selector
    fun evaluate(individual: I): F?

    // Evaluate multiple individuals. Override if it can be optimized
    fun evaluate(individuals: List<I>): List<F?> {
        return map(::evaluate, individuals)
    }

    fun select(list: List<IndividualWithFitness<I, F>>, k: Int): List<IndividualWithFitness<I, F>>

    fun mate(individual: I, individual2: I): Pair<I, I>

    // A simpler interface for the algorithms. Also check if the input does not change, and preserve the fitness in that case
    fun mateWithFitness(individual: IndividualWithFitness<I, F>, individual2: IndividualWithFitness<I, F>): Pair<IndividualWithFitness<I, F>, IndividualWithFitness<I, F>> {
        val (ni1, ni2) = mate(individual.individual, individual2.individual)

        return (if (ni1 === individual.individual) { individual } else { IndividualWithFitness(ni1, null as F) }) to
                (if (ni2 === individual2.individual) { individual2 } else { IndividualWithFitness(ni2, null as F) })
    }

    fun mutate(it: I): I

    // A simpler interface for the algorithms. Also check if the input does not change, and preserve the fitness in that case
    fun mutateWithFitness(individual: IndividualWithFitness<I, F>): IndividualWithFitness<I, F> {
        val mutated = mutate(individual.individual)
        if (mutated === individual.individual) {
            return individual
        }
        return IndividualWithFitness(mutated, null)
    }

    // Mapper is main multithreading entry point
    fun <T, R> map(mapper: (T) -> R, items: List<T>): List<R> {
        return items
            .parallelStream()
            .map(mapper)
            .collect(Collectors.toList())
    }

    // Callback to be called on each generation
    fun onGeneration(population: List<IndividualWithFitness<I, F>>)
}