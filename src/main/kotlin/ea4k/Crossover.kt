package ea4k

import java.lang.Integer.min
import kotlin.random.Random

/**
 * Executes a one point crossover on the input :term:`sequence` individuals.
 * Two new individual are returned. The resulting individuals will
 * respectively have the length of the original individual.
 *
 * @param ind1 The first individual participating in the crossover.
 * @param ind2 The second individual participating in the crossover.
 * @param random The random function to select the poind
 * @return A tuple of two individuals.
 */
fun <I> cxOnePoint(ind1: List<I>, ind2: List<I>): Pair<List<I>, List<I>> {
    val ind1mut = ind1.toMutableList()
    val ind2mut = ind2.toMutableList()

    val size = min(ind1mut.size, ind2mut.size)
    val cxpoint = Random.nextInt(0, size - 1)
    val ori1 = ind1mut[cxpoint]
    val ori2 = ind2mut[cxpoint]
    ind1mut[cxpoint] = ori2
    ind2mut[cxpoint] = ori1

    return ind1mut to ind2mut
}

/**
 * Executes a two-point crossover on the input :term:`sequence`
 * individuals. Two new individual are returned and with the same
 * length as their input
 *
 * @param ind1 The first individual participating in the crossover.
 * @param ind2 The second individual participating in the crossover.
 * @return A tuple of two individuals.
 */
fun <I> cxTwoPoint(ind1: List<I>, ind2: List<I>): Pair<List<I>, List<I>> {
    val ind1mut = ind1.toMutableList()
    val ind2mut = ind2.toMutableList()

    val size = min(ind1mut.size, ind2mut.size)

    var cxpoint1 = Random.nextInt(0, size - 1)
    var cxpoint2 = Random.nextInt(0, size - 2)
    if (cxpoint2 >= cxpoint1) {
        cxpoint2 += 1
    } else {
        //Swap the two cx points
        val temp = cxpoint1
        cxpoint1 = cxpoint2
        cxpoint2 = temp
    }

    (cxpoint1..cxpoint2).forEach {
        val temp = ind1mut[it]
        ind1mut[it] = ind2mut[it]
        ind2mut[it] = temp
    }

    return ind1mut to ind2mut
}

/**
 * Executes a uniform crossover between individuals.
 * The attributes are swapped according to the *indpb* probability.
 *
 * @param ind1 The first individual participating in the crossover.
 * @param ind2 The second individual participating in the crossover.
 * @param indpb Independent probability for each attribute to be exchanged.
 * @return A tuple of two individuals.
 */
fun <I> cxUniform(ind1: List<I>, ind2: List<I>, indpb: Double): Pair<List<I>, List<I>> {
    val ind1mut = ind1.toMutableList()
    val ind2mut = ind2.toMutableList()

    val size = min(ind1mut.size, ind2mut.size)
    (0..size-1).forEach {
        if (Random.nextDouble() < indpb) {
            val temp = ind1mut[it]
            ind1mut[it] = ind2mut[it]
            ind2mut[it] = temp
        }
    }

    return ind1mut to ind2mut
}
