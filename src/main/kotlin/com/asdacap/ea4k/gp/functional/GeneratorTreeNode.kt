package com.asdacap.ea4k.gp.functional

import com.asdacap.ea4k.gp.NodeType
import com.asdacap.ea4k.gp.TreeNode
import com.asdacap.ea4k.gp.TreeNodeFactory
import com.asdacap.ea4k.gp.Utils
import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.reflect

/**
 * A tree node that evaluate into a function that always return a constant which is assigned by the factory,
 * which contains a generator.
 */
class GeneratorTreeNode<I: Any, R>(
    val constant: R,
    override val factory: TreeNodeFactory<NodeFunction<I, R>>
): TreeNode<NodeFunction<I, R>>() {
    override val state: JsonNode by lazy {
        val value = Utils.objectMapper.createObjectNode()
        value.set<JsonNode>("constant", Utils.objectMapper.valueToTree(constant))
        value
    }

    override fun evaluate(): NodeFunction<I, R> {
        return ConstantNodeFunction(constant)
    }

    override fun replaceChildren(newChildren: List<TreeNode<*>>): TreeNode<NodeFunction<I, R>> {
        return GeneratorTreeNode(constant, factory)
    }

    /**
     * Generator can't use the FromFuncTreeNodeFactory as it need to store the generated value during serialization
     */
    class Factory<I: Any, R: Any>(
        val generator: () -> R,
        val kotlinReturnType: Class<*> = generator.reflect()!!.returnType.jvmErasure.java,
        override val returnType: NodeType,
    ) : TreeNodeFactory<NodeFunction<I, R>> {

        override fun createNode(children: List<TreeNode<*>>, state: JsonNode?): TreeNode<NodeFunction<I, R>> {
            val constant = if (state == null) {
                generator()
            } else {
                val constantState = state.get("constant")
                @Suppress("UNCHECKED_CAST")
                Utils.objectMapper.treeToValue(constantState, kotlinReturnType) as R
            }

            return GeneratorTreeNode(constant, this)
        }

        override val args: List<NodeType> = listOf()
    }
}

