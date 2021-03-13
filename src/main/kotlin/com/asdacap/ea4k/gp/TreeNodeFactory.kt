package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.JsonNode

/**
 * A factory for TreeNode. Also stores returnType and args which is used by generator and pset to create tree.
 */
interface TreeNodeFactory<out R> {
    val returnType: NodeType
    val args: List<NodeType>

    /**
     * Create a TreeNode with the given children and state. If state is null, create a new default TreeNode.
     */
    fun createNode(children: List<TreeNode<*>>, state: JsonNode? = null): TreeNode<R>
}
