package com.asdacap.ea4k.gp.functional

/**
 * A SAM that represent the callable function
 */
fun interface NodeFunction<out R> {
    fun call(ctx: CallCtx): R
}

/**
 * A NodeFunction that always return a constant
 */
class ConstantNodeFunction<R> (val constant: R): NodeFunction<R> {
    override fun call(ctx: CallCtx): R {
        return constant
    }
}

/**
 * Per tree evaluation, there is a context.
 * The context provide the arguments for the terminals.
 */
class CallCtx(
    // Array instead of List for optimization reason
    val args: Array<Any> = arrayOf()
)