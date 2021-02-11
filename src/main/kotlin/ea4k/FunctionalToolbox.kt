package ea4k

/**
 * Toolbox where each member implementation is given by a function
 */
class FunctionalToolbox<I: Individual<F>, F>(
    val evaluateFn: (I) -> F,
    val cloneFn: (I) -> I,
    val selectFn: (List<I>, Int) -> List<I>,
    val mateFn: (I, I) -> Pair<I, I>,
    val mutateFn: (I) -> I,
    val onGenerationFn: (List<I>) -> Unit = {},
): Toolbox<I, F> {
    override fun evaluate(individual: I): F {
        return evaluateFn(individual)
    }

    override fun clone(it: I): I {
        return cloneFn(it)
    }

    override fun select(list: List<I>, k: Int): List<I> {
        return selectFn(list, k)
    }

    override fun mate(individual: I, individual2: I): Pair<I, I> {
        return mateFn(individual, individual2)
    }

    override fun mutate(it: I): I {
        return mutateFn(it)
    }

    override fun onGeneration(population: List<I>) {
        return onGenerationFn(population)
    }
}

fun <I: Individual<F>, F> Toolbox<I, F>.withEvaluateFn(evaluateFn: (I) -> F): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        evaluateFn,
        this::clone,
        this::select,
        this::mate,
        this::mutate,
        this::onGeneration,
    )
}

fun <I: Individual<F>, F> Toolbox<I, F>.withCloneFn(cloneFn: (I) -> I): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        cloneFn,
        this::select,
        this::mate,
        this::mutate,
        this::onGeneration,
    )
}

fun <I: Individual<F>, F> Toolbox<I, F>.withSelect(selectFn: (List<I>, Int) -> List<I>): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        this::clone,
        selectFn,
        this::mate,
        this::mutate,
        this::onGeneration,
    )
}

fun <I: Individual<F>, F> Toolbox<I, F>.withMate(mateFn: (I, I) -> Pair<I, I>): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        this::clone,
        this::select,
        mateFn,
        this::mutate,
        this::onGeneration,
    )
}

fun <I: Individual<F>, F> Toolbox<I, F>.withMutate(mutateFn: (I) -> I): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        this::clone,
        this::select,
        this::mate,
        mutateFn,
        this::onGeneration,
    )
}

fun <I: Individual<F>, F> Toolbox<I, F>.withOnGeneration(onGenerationFn: (List<I>) -> Unit): FunctionalToolbox<I, F> {
    return FunctionalToolbox(
        this::evaluate,
        this::clone,
        this::select,
        this::mate,
        this::mutate,
        onGenerationFn,
    )
}
