package com.asdacap.ea4k.gp

import com.asdacap.ea4k.gp.Utils.objectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.random.Random.Default.nextDouble

/**
 * Stores a set of terminal and primitives
 */
class PSet<R>(val returnType: NodeType) {

    data class FactoryEntry(val name: String, val weight: Double, val factory: TreeNodeFactory<*>)

    val terminalRatio: Double
        get() {
            val terminalCount = terminals.size
            val primitiveCount = primitives.size
            return terminalCount.toDouble() / (primitiveCount + terminalCount).toDouble()
        }
    private val factories: MutableList<FactoryEntry> = mutableListOf()
    private val terminals: List<FactoryEntry> get() = factories.filter { it.factory.args.size == 0 }
    private val primitives: List<FactoryEntry> get() = factories.filter { it.factory.args.size != 0 }

    fun <R> addTreeNodeFactory(name: String, primitive: TreeNodeFactory<R>, weight: Double = 1.0) {
        factories.add(0, FactoryEntry(name, weight, primitive))
    }

    fun <R> serialize(tree: TreeNode<R>): JsonNode {
        val factory = factories.find {
            it.factory == tree.factory
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
        json.put("factory", factory.name)
        if (parent != objectMapper.nullNode()) {
            json.set<ObjectNode>("node", parent)
        }
        if (!childArray.isEmpty) {
            json.set<ObjectNode>("children", childArray)
        }
        return json
    }

    fun deserialize(jsonNode: JsonNode): TreeNode<*> {
        val factoryName = jsonNode.get("factory").asText()!!

        val factory = factories.find {
            it.name == factoryName
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

        val nodeInfo = jsonNode.get("node") ?: null
        return factory.factory.createNode(children, nodeInfo)
    }

    fun getTerminalsAssignableTo(ret: NodeType): List<TreeNodeFactory<*>> {
        return terminals.filter { it.factory.returnType.isAssignableTo(ret) } .map { it.factory }
    }

    fun getPrimitivesAssignableTo(ret: NodeType): List<TreeNodeFactory<*>> {
        return primitives.filter { it.factory.returnType.isAssignableTo(ret) } .map { it.factory }
    }

    fun selectTerminalAssignableTo(ret: NodeType): TreeNodeFactory<*>? {
        val terminals = this.terminals.toList()
        val totalWeight = terminals.map { it.weight }.sum()

        var randomNumber = nextDouble() * totalWeight
        var cumulative = 0.0;
        terminals.forEach {
            cumulative = cumulative + it.weight
            if (cumulative > randomNumber) {
                return it.factory
            }
        }
        return null
    }

    fun selectPrimitiveAssignableTo(ret: NodeType): TreeNodeFactory<*>? {
        val primitives = this.primitives.toList()
        val totalWeight = primitives.map { it.weight }.sum()

        var randomNumber = nextDouble() * totalWeight
        var cumulative = 0.0;
        primitives.forEach {
            cumulative = cumulative + it.weight
            if (cumulative > randomNumber) {
                return it.factory
            }
        }
        return null
    }
}