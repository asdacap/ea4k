package com.asdacap.ea4k.gp

/**
 * A SAM that represent the callable function
 */
fun interface Function<out R> {
    fun call(ctx: CallCtx): R
}

/**
 * Per tree evaluation, there is a context.
 * The context provide the arguments for the terminals.
 */
class CallCtx(
    // Array instead of List for optimization reason
    val args: Array<Any> = arrayOf()
)