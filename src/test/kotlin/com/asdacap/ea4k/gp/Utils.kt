package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertTrue

fun <R> testCommonNodeBehaviour(factory: TreeNodeFactory<R>, sampleChildren: List<BaseTreeNode<*>>) {
    val node = factory.createNode(sampleChildren)
    val objectMapper = ObjectMapper()
    val serialized = factory.serialize(node, objectMapper)
    val deserialied = factory.deserialize(serialized, sampleChildren)
    assertTrue(node.isSubtreeEffectivelySame(deserialied))
}

