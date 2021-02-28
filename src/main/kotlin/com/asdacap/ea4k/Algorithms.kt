package com.asdacap.ea4k

import kotlin.random.Random

/*
    Part of an evolutionary algorithm applying only the variation part
    (crossover, mutation **or** reproduction). The modified individuals have
    their fitness invalidated. The individuals are cloned so returned
    population is independent of the input population.
    :param population: A list of individuals to vary.
    :param toolbox: A :class:`~deap.base.Toolbox` that contains the evolution
                    operators.
    :param lambda\_: The number of children to produce
    :param cxpb: The probability of mating two individuals.
    :param mutpb: The probability of mutating an individual.
    :returns: The final population.
    The variation goes as follow. On each of the *lambda_* iteration, it
    selects one of the three operations; crossover, mutation or reproduction.
    In the case of a crossover, two individuals are selected at random from
    the parental population :math:`P_\mathrm{p}`, those individuals are cloned
    using the :meth:`toolbox.clone` method and then mated using the
    :meth:`toolbox.mate` method. Only the first child is appended to the
    offspring population :math:`P_\mathrm{o}`, the second child is discarded.
    In the case of a mutation, one individual is selected at random from
    :math:`P_\mathrm{p}`, it is cloned and then mutated using using the
    :meth:`toolbox.mutate` method. The resulting mutant is appended to
    :math:`P_\mathrm{o}`. In the case of a reproduction, one individual is
    selected at random from :math:`P_\mathrm{p}`, cloned and appended to
    :math:`P_\mathrm{o}`.
    This variation is named *Or* because an offspring will never result from
    both operations crossover and mutation. The sum of both probabilities
    shall be in :math:`[0, 1]`, the reproduction probability is
    1 - *cxpb* - *mutpb*.
 */
fun <I, F> varOr(
    population: List<IndividualWithFitness<I, F>>,
    toolbox: Toolbox<I, F>,
    lambda_: Int,
    cxpb: Float,
    mutpb: Float
): List<IndividualWithFitness<I, F>> {
    check(cxpb + mutpb <= 1.0f, { "The sum of the crossover and mutation probabilities must be smaller or equal to 1.0." })

    return (0..lambda_).let {
        toolbox.map({
            val opChoice = Random.nextFloat()
            if (opChoice < cxpb) {
                // Crossover
                val rand1 = population[Random.nextInt(0, population.size)]
                val rand2 = population[Random.nextInt(0, population.size)]
                toolbox.mateWithFitness(rand1, rand2).first
            } else if (opChoice < cxpb + mutpb) {
                // Mutation
                val ind = population[Random.nextInt(0, population.size)]
                toolbox.mutateWithFitness(ind)
            } else {
                // Reproduction
                population[Random.nextInt(0, population.size)]
            }
        }, it.toList())
    }
}

/*
    Part of an evolutionary algorithm applying only the variation part
    (crossover **and** mutation). The modified individuals have their
    fitness invalidated. The individuals are cloned so returned population is
    independent of the input population.
    :param population: A list of individuals to vary.
    :param toolbox: A :class:`~deap.base.Toolbox` that contains the evolution
                    operators.
    :param cxpb: The probability of mating two individuals.
    :param mutpb: The probability of mutating an individual.
    :returns: A list of varied individuals that are independent of their
              parents.
    The variation goes as follow. First, the parental population
    :math:`P_\mathrm{p}` is duplicated using the :meth:`toolbox.clone` method
    and the result is put into the offspring population :math:`P_\mathrm{o}`.  A
    first loop over :math:`P_\mathrm{o}` is executed to mate pairs of
    consecutive individuals. According to the crossover probability *cxpb*, the
    individuals :math:`\mathbf{x}_i` and :math:`\mathbf{x}_{i+1}` are mated
    using the :meth:`toolbox.mate` method. The resulting children
    :math:`\mathbf{y}_i` and :math:`\mathbf{y}_{i+1}` replace their respective
    parents in :math:`P_\mathrm{o}`. A second loop over the resulting
    :math:`P_\mathrm{o}` is executed to mutate every individual with a
    probability *mutpb*. When an individual is mutated it replaces its not
    mutated version in :math:`P_\mathrm{o}`. The resulting :math:`P_\mathrm{o}`
    is returned.
    This variation is named *And* because of its propensity to apply both
    crossover and mutation on the individuals. Note that both operators are
    not applied systematically, the resulting individuals can be generated from
    crossover only, mutation only, crossover and mutation, and reproduction
    according to the given probabilities. Both probabilities should be in
    :math:`[0, 1]`.
 */
fun <I, F> varAnd(
    population: List<IndividualWithFitness<I, F>>,
    toolbox: Toolbox<I, F>,
    cxpb: Float,
    mutpb: Float
): List<IndividualWithFitness<I, F>> {
    val asMut = population.toMutableList()

    (0..(asMut.size-2) step 2).forEach{
        if (Random.nextFloat() < cxpb) {
            val (m1, m2) = toolbox.mateWithFitness(asMut[it], asMut[it+1])
            asMut[it] = m1
            asMut[it+1] = m2
        }
    }

    (0..(asMut.size-1)).forEach{
        if (Random.nextFloat() < mutpb) {
            val m1 = toolbox.mutateWithFitness(asMut[it])
            asMut[it] = m1
        }
    }

    return asMut
}

// Evaluate the individual pair with an invalid fitness
fun <I, F> evaluateInvalid(population: List<IndividualWithFitness<I, F>>, toolbox: Toolbox<I, F>): List<IndividualWithFitness<I, F>> {
    val asMut = population.toMutableList()

    // Evaluate the individuals with an invalid fitness
    val invalidIndividual = asMut.mapIndexed { idx, it ->
        idx to it
    }.filter {
        it.second.fitness == null
    }

    toolbox.evaluate(invalidIndividual.map { it.second.individual })
        .zip(invalidIndividual).forEach {
            asMut[it.second.first] = IndividualWithFitness(it.second.second.individual, it.first)
        }

    return asMut
}

/*
    This is the :math:`(\mu + \lambda)` evolutionary algorithm.
    :param population: A list of individuals.
    :param toolbox: A :class:`~deap.base.Toolbox` that contains the evolution
                    operators.
    :param mu: The number of individuals to select for the next generation.
    :param lambda\_: The number of children to produce at each generation.
    :param cxpb: The probability that an offspring is produced by crossover.
    :param mutpb: The probability that an offspring is produced by mutation.
    :param ngen: The number of generation.
    :param stats: A :class:`~deap.tools.Statistics` object that is updated
                  inplace, optional.
    :param halloffame: A :class:`~deap.tools.HallOfFame` object that will
                       contain the best individuals, optional.
    :param verbose: Whether or not to log the statistics.
    :returns: The final population
    :returns: A class:`~deap.tools.Logbook` with the statistics of the
              evolution.
    The algorithm takes in a population and evolves it in place using the
    :func:`varOr` function. It returns the optimized population and a
    :class:`~deap.tools.Logbook` with the statistics of the evolution. The
    logbook will contain the generation number, the number of evaluations for
    each generation and the statistics if a :class:`~deap.tools.Statistics` is
    given as argument. The *cxpb* and *mutpb* arguments are passed to the
    :func:`varOr` function. The pseudocode goes as follow ::
        evaluate(population)
        for g in range(ngen):
            offspring = varOr(population, toolbox, lambda_, cxpb, mutpb)
            evaluate(offspring)
            population = select(population + offspring, mu)
    First, the individuals having an invalid fitness are evaluated. Second,
    the evolutionary loop begins by producing *lambda_* offspring from the
    population, the offspring are generated by the :func:`varOr` function. The
    offspring are then evaluated and the next generation population is
    selected from both the offspring **and** the population. Finally, when
    *ngen* generations are done, the algorithm returns a tuple with the final
    population and a :class:`~deap.tools.Logbook` of the evolution.
    This function expects :meth:`toolbox.mate`, :meth:`toolbox.mutate`,
    :meth:`toolbox.select` and :meth:`toolbox.evaluate` aliases to be
    registered in the toolbox. This algorithm uses the :func:`varOr`
    variation.
*/
fun <I, F> eaMuPlusLambda(
    population: List<IndividualWithFitness<I, F>>,
    toolbox: Toolbox<I, F>,
    mu: Int,
    lambda_: Int,
    cxpb: Float,
    mutpb: Float,
    ngen: Int
): List<IndividualWithFitness<I, F>> {

    var population = population

    population = evaluateInvalid(population, toolbox)

    toolbox.onGeneration(population)

    (1..(ngen+1)).forEach {
        var offspring = varOr(
            population,
            toolbox,
            lambda_,
            cxpb,
            mutpb)

        offspring = evaluateInvalid(offspring, toolbox)

        // Select the next generation population
        population = toolbox.select(population + offspring, mu)
        toolbox.onGeneration(population)
    }

    return population
}

/*
    This algorithm reproduce the simplest evolutionary algorithm as
    presented in chapter 7 of [Back2000]_.
    :param population: A list of individuals.
    :param toolbox: A :class:`~deap.base.Toolbox` that contains the evolution
                    operators.
    :param cxpb: The probability of mating two individuals.
    :param mutpb: The probability of mutating an individual.
    :param ngen: The number of generation.
    :param stats: A :class:`~deap.tools.Statistics` object that is updated
                  inplace, optional.
    :param halloffame: A :class:`~deap.tools.HallOfFame` object that will
                       contain the best individuals, optional.
    :param verbose: Whether or not to log the statistics.
    :returns: The final population
    :returns: A class:`~deap.tools.Logbook` with the statistics of the
              evolution
    The algorithm takes in a population and evolves it in place using the
    :meth:`varAnd` method. It returns the optimized population and a
    :class:`~deap.tools.Logbook` with the statistics of the evolution. The
    logbook will contain the generation number, the number of evaluations for
    each generation and the statistics if a :class:`~deap.tools.Statistics` is
    given as argument. The *cxpb* and *mutpb* arguments are passed to the
    :func:`varAnd` function. The pseudocode goes as follow ::
        evaluate(population)
        for g in range(ngen):
            population = select(population, len(population))
            offspring = varAnd(population, toolbox, cxpb, mutpb)
            evaluate(offspring)
            population = offspring
    As stated in the pseudocode above, the algorithm goes as follow. First, it
    evaluates the individuals with an invalid fitness. Second, it enters the
    generational loop where the selection procedure is applied to entirely
    replace the parental population. The 1:1 replacement ratio of this
    algorithm **requires** the selection procedure to be stochastic and to
    select multiple times the same individual, for example,
    :func:`~deap.tools.selTournament` and :func:`~deap.tools.selRoulette`.
    Third, it applies the :func:`varAnd` function to produce the next
    generation population. Fourth, it evaluates the new individuals and
    compute the statistics on this population. Finally, when *ngen*
    generations are done, the algorithm returns a tuple with the final
    population and a :class:`~deap.tools.Logbook` of the evolution.
    .. note::
        Using a non-stochastic selection method will result in no selection as
        the operator selects *n* individuals from a pool of *n*.
    This function expects the :meth:`toolbox.mate`, :meth:`toolbox.mutate`,
    :meth:`toolbox.select` and :meth:`toolbox.evaluate` aliases to be
    registered in the toolbox.
    .. [Back2000] Back, Fogel and Michalewicz, "Evolutionary Computation 1 :
       Basic Algorithms and Operators", 2000.
*/
fun <I, F> eaSimple(
    population: List<IndividualWithFitness<I, F>>,
    toolbox: Toolbox<I, F>,
    cxpb: Float,
    mutpb: Float,
    ngen: Int
): List<IndividualWithFitness<I, F>> {
    var population = population

    // Evaluate the individuals with an invalid fitness
    population = evaluateInvalid(population, toolbox)

    toolbox.onGeneration(population)

    (1..(ngen+1)).forEach {
        // Select the next generation population
        var offspring = toolbox.select(population, population.size)

        offspring = varAnd(offspring, toolbox, cxpb, mutpb)

        offspring = evaluateInvalid(offspring, toolbox)

        population = offspring

        // Update the hall of fame with the generated individuals
        toolbox.onGeneration(offspring)
    }

    return population
}


/*
def eaMuCommaLambda(population, toolbox, mu, lambda_, cxpb, mutpb, ngen,
                    stats=None, halloffame=None, verbose=__debug__):
    """This is the :math:`(\mu~,~\lambda)` evolutionary algorithm.
    :param population: A list of individuals.
    :param toolbox: A :class:`~deap.base.Toolbox` that contains the evolution
                    operators.
    :param mu: The number of individuals to select for the next generation.
    :param lambda\_: The number of children to produce at each generation.
    :param cxpb: The probability that an offspring is produced by crossover.
    :param mutpb: The probability that an offspring is produced by mutation.
    :param ngen: The number of generation.
    :param stats: A :class:`~deap.tools.Statistics` object that is updated
                  inplace, optional.
    :param halloffame: A :class:`~deap.tools.HallOfFame` object that will
                       contain the best individuals, optional.
    :param verbose: Whether or not to log the statistics.
    :returns: The final population
    :returns: A class:`~deap.tools.Logbook` with the statistics of the
              evolution
    The algorithm takes in a population and evolves it in place using the
    :func:`varOr` function. It returns the optimized population and a
    :class:`~deap.tools.Logbook` with the statistics of the evolution. The
    logbook will contain the generation number, the number of evaluations for
    each generation and the statistics if a :class:`~deap.tools.Statistics` is
    given as argument. The *cxpb* and *mutpb* arguments are passed to the
    :func:`varOr` function. The pseudocode goes as follow ::
        evaluate(population)
        for g in range(ngen):
            offspring = varOr(population, toolbox, lambda_, cxpb, mutpb)
            evaluate(offspring)
            population = select(offspring, mu)
    First, the individuals having an invalid fitness are evaluated. Second,
    the evolutionary loop begins by producing *lambda_* offspring from the
    population, the offspring are generated by the :func:`varOr` function. The
    offspring are then evaluated and the next generation population is
    selected from **only** the offspring. Finally, when
    *ngen* generations are done, the algorithm returns a tuple with the final
    population and a :class:`~deap.tools.Logbook` of the evolution.
    .. note::
        Care must be taken when the lambda:mu ratio is 1 to 1 as a
        non-stochastic selection will result in no selection at all as the
        operator selects *lambda* individuals from a pool of *mu*.
    This function expects :meth:`toolbox.mate`, :meth:`toolbox.mutate`,
    :meth:`toolbox.select` and :meth:`toolbox.evaluate` aliases to be
    registered in the toolbox. This algorithm uses the :func:`varOr`
    variation.
    """
    */
fun <I, F> eaMuCommaLambda(
    population: List<IndividualWithFitness<I, F>>,
    toolbox: Toolbox<I, F>,
    mu: Int,
    lambda_: Int,
    cxpb: Float,
    mutpb: Float,
    ngen: Int
): List<IndividualWithFitness<I, F>> {
    check(lambda_ >= mu, { "lambda must be greater or equal to mu." })

    var population = population
    population = evaluateInvalid(population, toolbox)

    toolbox.onGeneration(population)

    (1..ngen).forEach {
        var offspring = varOr(population, toolbox, lambda_, cxpb, mutpb)

        offspring = evaluateInvalid(offspring, toolbox)

        // Select next generation
        population = toolbox.select(offspring, mu)

        toolbox.onGeneration(population)
    }

    return population
}
