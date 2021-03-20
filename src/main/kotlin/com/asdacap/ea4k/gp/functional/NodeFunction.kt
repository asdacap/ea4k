package com.asdacap.ea4k.gp.functional

/**
 * A SAM that represent the callable function
 */
fun interface NodeFunction<in I, out R> {
    fun call(input: I): R
}

/**
 * A NodeFunction that always return a constant
 */
open class ConstantNodeFunction<R> (val constant: R): NodeFunction<Any, R> {
    override fun call(input: Any): R {
        return constant
    }
}

/**
 * Per tree evaluation, there is a context.
 * The context provide the arguments for the terminals.
 */
open class CallCtx(
    vararg val args: Any
)