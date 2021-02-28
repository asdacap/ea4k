package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.reflect.typeOf

class GeneratorTerminalFactoryTest {
    @Test
    fun testGeneratorDetectReturnTypeCorrectly() {
        val factory = GeneratorTerminalFactory({ -1f })
        Assertions.assertEquals(typeOf<Float>(), factory.returnType)
    }

    @Test
    fun testGeneratorDetectReturnTypeCorrectly2() {
        val factory = GeneratorTerminalFactory({ true })
        Assertions.assertEquals(typeOf<Boolean>(), factory.returnType)
    }

    @Test
    fun testCommonBehaviour() {
        val factory = GeneratorTerminalFactory({ Random.nextFloat() })
        testCommonNodeBehaviour(factory, listOf())
        val node = factory.createNode(listOf())
        assertTrue(factory.canSerialize(node))
        val afterSerialize = node
            .let { factory.serialize(it, ObjectMapper()) }
            .let { factory.deserialize(it, listOf()) }

        assertTrue(node.isSubtreeEffectivelySame(afterSerialize))
    }
}