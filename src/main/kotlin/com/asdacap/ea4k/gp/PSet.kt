package com.asdacap.ea4k.gp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * Stores a set of terminal and primitives
 */
class PSet<R>(val returnType: NodeType) {
    val terminalRatio: Float
        get() {
            val terminalCount = terminals.map { it.value.size }.sum()
            val primitiveCount = primitives.map { it.value.size }.sum()
            return terminalCount.toFloat() / (primitiveCount + terminalCount).toFloat()
        }
    private val terminals: MutableMap<NodeType, MutableList<TreeNodeFactory<*>>> = mutableMapOf()
    private val primitives: MutableMap<NodeType, MutableList<TreeNodeFactory<*>>> = mutableMapOf()
    private val serializers: MutableList<Pair<String, TreeNodeFactory<*>>> = mutableListOf()

    private fun <R> addTerminal(name: String, terminal: TreeNodeFactory<R>) {
        if (terminals[terminal.returnType] == null) {
            terminals[terminal.returnType] = mutableListOf()
        }
        terminals[terminal.returnType]?.add(terminal)
        serializers.add(0, name to terminal)
    }

    private fun <R> addPrimitive(name: String, primitive: TreeNodeFactory<R>) {
        if (primitives[primitive.returnType] == null) {
            primitives[primitive.returnType] = mutableListOf()
        }
        primitives[primitive.returnType]?.add(primitive)
        serializers.add(0, name to primitive)
    }

    fun <R> addTreeNodeFactory(name: String, primitive: TreeNodeFactory<R>) {
        if (primitive.args.size == 0) {
            addTerminal(name, primitive)
        } else {
            addPrimitive(name, primitive)
        }
    }

    val objectMapper = ObjectMapper()

    fun <R> serialize(tree: TreeNode<R>): JsonNode {
        val factory = serializers.find {
            it.second == tree.factory
        }

        if (factory == null) {
            throw Exception("Cant find factory for tree of type: " + tree.javaClass.canonicalName)
        }

        val parent = tree.state
        val childs = tree.children.map { serialize(it) }

        val json = objectMapper.createObjectNode()
        val childArray = objectMapper.createArrayNode()
        childs.forEach {
            childArray.add(it)
        }
        json.put("factory", factory.first)
        if (parent != objectMapper.createObjectNode()) {
            json.set<ObjectNode>("node", parent)
        }
        if (!childArray.isEmpty) {
            json.set<ObjectNode>("children", childArray)
        }
        return json
    }

    fun deserialize(jsonNode: JsonNode): TreeNode<*> {
        val factoryName = jsonNode.get("factory").asText()!!

        val factory = serializers.find {
            it.first == factoryName
        }
        if (factory == null) {
            throw Exception("Unknown factory $factoryName")
        }

        val children = jsonNode.get("children")
            .let {
                it ?: objectMapper.createArrayNode()
            }
            .asIterable().map {
                deserialize(it)
            }

        val nodeInfo = jsonNode.get("node") ?: objectMapper.createObjectNode()
        return factory.second.createNode(children, nodeInfo)
    }

    fun getTerminalAssignableTo(ret: NodeType): List<TreeNodeFactory<*>> {
        return terminals.filter { it.key.isAssignableTo(ret) } .flatMap { it.value }
    }

    fun getPrimitiveAssignableTo(ret: NodeType): List<TreeNodeFactory<*>> {
        return primitives.filter { it.key.isAssignableTo(ret) } .flatMap { it.value }
    }
}