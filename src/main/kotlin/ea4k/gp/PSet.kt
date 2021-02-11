package ea4k.gp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.reflect.KType


/**
 * Stores a set of terminal and primitives
 */
class PSet<R>(val returnType: KType, args: List<KType>) {
    val terminalRatio: Float
        get() {
            val terminalCount = terminals.map { it.value.size }.sum()
            val primitiveCount = primitives.map { it.value.size }.sum()
            return terminalCount.toFloat() / (primitiveCount + terminalCount).toFloat()
        }
    val terminals: MutableMap<KType, MutableList<TreeNodeFactory<*>>> = mutableMapOf()
    val primitives: MutableMap<KType, MutableList<TreeNodeFactory<*>>> = mutableMapOf()
    val serializers: MutableList<Pair<String, TreeNodeFactory<*>>> = mutableListOf()
    val rootTreeNodeFactory = RootTreeNodeFactory<R>(returnType)

    init {
        serializers.add("root" to rootTreeNodeFactory)
        args.forEachIndexed{ i, it ->
            addTerminal("ARG"+i.toString(), FromFuncTreeNodeFactory.fromArgs<Any>(i, it))
        }
    }

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

    fun <R> serializeToJson(tree: BaseTreeNode<R>): JsonNode {
        val factory = serializers.find {
            it.second.canSerialize(tree)
        }

        if (factory == null) {
            throw Exception("Cant find factory for tree of type: " + tree.javaClass.canonicalName)
        }

        val parent = (factory.second as TreeNodeFactory<R>).serialize(tree, objectMapper)
        val childs = tree.children.map { serializeToJson(it) }

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

    fun deserialize(jsonNode: JsonNode): BaseTreeNode<*> {
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
        return factory.second.deserialize(nodeInfo, children)
    }

    fun wrapAsRoot(treeGen: BaseTreeNode<*>): BaseTreeNode<*> {
        return rootTreeNodeFactory.createNode(listOf(treeGen))
    }
}

