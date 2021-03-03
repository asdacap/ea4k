package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Something that makes treenode. This is what stored in pset.
 *
 * The problem is serialization.
 *
 * To be deserilized, it needs a name that the PSet can lookup.
 * Ideally, the factory-node combo does not need to know about the name.
 * But that means that the node need to have an association with the factory when created.
 * But that also means that we can't create a TreeNode just by itself, it needs to have a factory.
 *
 * This means that a lot of the optimization that creates a constant or a custom tree node will also need a factory
 * Ideally we do want to serialize the optimized tree, but we dont have to deserialize it.
 *
 * In some way, assuming all logic is in the tree node itself, we just need to fix the serializer. How do we register
 * a serializer?
 *
 * Additionally, a lot of node type does not have any serialization by itself. It need a factory, which
 * can recreate it.
 *
 */
interface TreeNodeFactory<out R>: TreeNodeSerializer {
    val returnType: NodeType
    val args: List<NodeType>

    fun createNode(children: List<BaseTreeNode<*>>): BaseTreeNode<R>
}

interface TreeNodeSerializer {
    fun serialize(tree: BaseTreeNode<*>, objectMapper: ObjectMapper): JsonNode
    fun deserialize(nodeInfo: JsonNode, children: List<BaseTreeNode<*>>): BaseTreeNode<*>
}
