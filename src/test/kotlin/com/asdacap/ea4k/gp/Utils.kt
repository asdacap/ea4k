package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertTrue

fun <R> testCommonNodeBehaviour(factory: TreeNodeFactory<R>, sampleChildren: List<TreeNode<*>>) {
    val node = factory.createNode(sampleChildren)
    val cloned = factory.createNode(node.children, node.state)
    assertTrue(node.isSubtreeEffectivelySame(cloned))
}

