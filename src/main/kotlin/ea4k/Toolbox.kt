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

    fun select(list: List<IndividualWithFitness<I, F>>, k: Int): List<IndividualWithFitness<I, F>>

    fun mate(individual: I, individual2: I): Pair<I, I>

    fun mutate(it: I): I

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