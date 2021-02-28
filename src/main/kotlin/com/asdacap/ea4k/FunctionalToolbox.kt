package com.asdacap.ea4k

/**
 * Toolbox where each member implementation is given by a function
 */
class FunctionalToolbox<I, F>(
    val evaluateFn: (I) -> F?,
    val selectFn: (List<IndividualWithFitness<I,F>>, Int) -> List<IndividualWithFitness<I, F>>,
    val mateFn: (I, I) -> Pair<I, I> = { id1, id2 -> id1 to id2 },
    val mutateFn: (I) -> I = { it },
    val onGenerationFn: (List<IndividualWithFitness<I, F>>) -> Unit = {},
): Toolbox<I, F> {
    override fun evaluate(individual: I): F? {
        return evaluateFn(individual)
    }

    override fun select(list: List<IndividualWithFitness<I, F>>, k: Int): List<IndividualWithFitness<I, F>> {
        return selectFn(list, k)
    }

    override fun mate(individual: I, individual2: I): Pair<I, I> {
        return mateFn(individual, individual2)
    }

    override fun mutate(it: I): I {
        return mutateFn(it)
    }

    override fun onGeneration(population: List<IndividualWithFitness<I, F>>) {
        return onGenerationFn(population)
    }
}

fun <I, F> Toolbox<I, F>.withEvaluate(evaluateFn: (I) -> F): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        evaluateFn,
        this::select,
        this::mate,
        this::mutate,
        this::onGeneration,
    )
}

fun <I, F> Toolbox<I, F>.withSelect(selectFn: (List<IndividualWithFitness<I, F>>, Int) -> List<IndividualWithFitness<I, F>>): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        selectFn,
        this::mate,
        this::mutate,
        this::onGeneration,
    )
}

fun <I, F> Toolbox<I, F>.withMate(mateFn: (I, I) -> Pair<I, I>): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        this::select,
        mateFn,
        this::mutate,
        this::onGeneration,
    )
}

fun <I, F> Toolbox<I, F>.withMutate(mutateFn: (I) -> I): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        this::select,
        this::mate,
        mutateFn,
        this::onGeneration,
    )
}

fun <I, F> Toolbox<I, F>.withOnGeneration(onGenerationFn: (List<IndividualWithFitness<I, F>>) -> Unit): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        this::select,
        this::mate,
        this::mutate,
        onGenerationFn,
    )
}
