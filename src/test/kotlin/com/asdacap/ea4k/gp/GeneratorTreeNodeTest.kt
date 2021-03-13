package com.asdacap.ea4k.gp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.reflect.typeOf

class GeneratorTreeNodeTest {
    @Test
    fun testGeneratorDetectReturnTypeCorrectly() {
        val factory = GeneratorTreeNode.Factory({ -1f })
        Assertions.assertEquals(KotlinNodeType(typeOf<Float>()), factory.returnType)
    }

    @Test
    fun testGeneratorDetectReturnTypeCorrectly2() {
        val factory = GeneratorTreeNode.Factory({ true })
        Assertions.assertEquals(KotlinNodeType(typeOf<Boolean>()), factory.returnType)
    }

    @Test
    fun testCommonBehaviour() {
        val factory = GeneratorTreeNode.Factory({ Random.nextFloat() })
        testCommonNodeBehaviour(factory, listOf())
    }
}