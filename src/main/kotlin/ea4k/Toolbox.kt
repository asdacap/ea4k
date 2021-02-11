package ea4k

import java.util.stream.Collectors

/**
 * Basically its a collection of configuration
 * that algorithms typically use
 */
interface Toolbox<I : Individual<F>, F> {
    // Evaluate the individual
    fun evaluate(individual: I): F

    // Clone the individual
    fun clone(it: I): I

    fun select(list: List<I>, k: Int): List<I>

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
    fun onGeneration(population: List<I>)
}