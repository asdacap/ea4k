package com.asdacap.ea4k

import com.asdacap.ea4k.Utils.randomChoice

object Selection {
    /*
        Select the best individual among *tournsize* randomly chosen
        individuals, *k* times. The list returned contains
        references to the input *individuals*.
        :param individuals: A list of individuals to select from.
        :param k: The number of individuals to select.
        :param tournsize: The number of individuals participating in each tournament.
        :param fit_attr: The attribute of individuals to use as selection criterion
        :returns: A list of selected individuals.
        This function uses the :func:`~random.choice` function from the python base
        :mod:`random` module.
     */
    fun <I> selTournament(individuals: List<I>, k: Int, tournsize: Int, fitness: Comparator<I>): List<I> {
        check(k > 0)
        check(tournsize > 0)

        return (0..k).map {
            val aspirants = (1..tournsize).map{ randomChoice(individuals) }
            aspirants.maxWithOrNull(fitness)!!
        }
    }

}
