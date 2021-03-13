package com.asdacap.ea4k

import kotlin.random.Random

object Utils {
    fun <R> randomChoice(list: List<R>): R {
        return list[Random.nextInt(0, list.size)]
    }
}