package ea4k

import java.lang.Integer.min
import kotlin.random.Random

/**
"""Executes a one point crossover on the input :term:`sequence` individuals.
The two individuals are modified in place. The resulting individuals will
respectively have the length of the other.
:param ind1: The first individual participating in the crossover.
:param ind2: The second individual participating in the crossover.
:returns: A tuple of two individuals.
This function uses the :func:`~random.randint` function from the
python base :mod:`random` module.
"""
 */
fun <I> cxOnePoint(ind1: MutableList<I>, ind2: MutableList<I>): Pair<MutableList<I>, MutableList<I>> {
    val size = min(ind1.size, ind2.size)
    val cxpoint = Random.nextInt(1, size - 1)
    val ori1 = ind1[cxpoint]
    val ori2 = ind2[cxpoint]
    ind1[cxpoint] = ori2
    ind2[cxpoint] = ori1

    return ind1 to ind2
}

/**
"""Executes a two-point crossover on the input :term:`sequence`
individuals. The two individuals are modified in place and both keep
their original length.
:param ind1: The first individual participating in the crossover.
:param ind2: The second individual participating in the crossover.
:returns: A tuple of two individuals.
This function uses the :func:`~random.randint` function from the Python
base :mod:`random` module.
"""
 */
fun <I> cxTwoPoint(ind1: MutableList<I>, ind2: MutableList<I>): Pair<MutableList<I>, MutableList<I>> {
    val size = min(ind1.size, ind2.size)

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
        val temp = ind1[it]
        ind1[it] = ind2[it]
        ind2[it] = temp
    }

    return ind1 to ind2
}

/**
"""Executes a uniform crossover that modify in place the two
:term:`sequence` individuals. The attributes are swapped according to the
 *indpb* probability.
:param ind1: The first individual participating in the crossover.
:param ind2: The second individual participating in the crossover.
:param indpb: Independent probability for each attribute to be exchanged.
:returns: A tuple of two individuals.
This function uses the :func:`~random.random` function from the python base
:mod:`random` module.
"""
 */
fun <I> cxUniform(ind1: MutableList<I>, ind2: MutableList<I>, indpb: Double): Pair<MutableList<I>, MutableList<I>> {
    val size = min(ind1.size, ind2.size)
    (0..size-1).forEach {
        if (Random.nextDouble() < indpb) {
            val temp = ind1[it]
            ind1[it] = ind2[it]
            ind2[it] = temp
        }
    }

    return ind1 to ind2
}
