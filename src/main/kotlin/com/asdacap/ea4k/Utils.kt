package com.asdacap.ea4k

import kotlin.random.Random

object Utils {
    fun <R> randomChoice(list: List<R>): R {
        return list[Random.nextInt(0, list.size)]
    }

    /**
     * Create a mate function from a base mate function which will replace the child with parent
     * if it does not pass `filter`.
     */
    fun <I> mateCutoff(base: (I, I) -> Pair<I, I>, filter: (I) -> Boolean): (I, I) -> Pair<I, I> {
        return { i1, i2 ->
            val (c1, c2) = base(i1, i2)

            Pair(
                if (filter(c1)) c1 else i1,
                if (filter(c2)) c2 else i2,
            )
        }
    }

    /**
     * Create a mutate function from a base mutate function which will replace the child with parent
     * if it does not pass `filter`.
     */
    fun <I> mutateCutoff(base: (I) -> I, filter: (I) -> Boolean): (I) -> I {
        return {
            val child = base(it)

            if (filter(child)) {
                child
            } else {
                it
            }
        }
    }
}