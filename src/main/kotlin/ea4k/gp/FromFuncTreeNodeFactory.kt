package ea4k.gp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KCallable
import kotlin.reflect.KType
import kotlin.reflect.jvm.reflect
import kotlin.reflect.typeOf

/**
 * Generate a tree node that essentially call the given function.
 *
 * Turns out, a lot of other type of tree node can be made with this factory.
 * So most tree node type basically translate back to this tree node
 */
class FromFuncTreeNodeFactory <R>(
    // The given function accept the call context and an array of child tree node.
    // The implementation may skip calling she child tree node for optimization purpose.
    val func: (CallCtx, Array<BaseTreeNode<*>>) -> R,

    // The desired type of child for this tree node type
    override val args: List<KType>,

    // The return type of this tree node type.
    // If not defined it may try to use reflection to detect it, but it does not work when passing lambda.
    override val returnType: KType = func.reflect()!!.returnType,

    // A tree shaker for optimization purpose.
    val treeOptimizer: TreeOptimizer<R> = { it },

    // Set to false if this treeNode may not always return the same result when its children return the same result
    val isPure: Boolean = true
): TreeNodeFactory<R> {

    companion object {

        /**
         * Create a tree node factory from a kotlin KCallable. This will use reflection to detect the callable's
         * argument and return type.
         */
        inline fun <reified R> fromFunction(func: KCallable<R>, isPure: Boolean = true): FromFuncTreeNodeFactory<R> {
            return FromFuncTreeNodeFactory({ ctx, children ->
                // This is a highly hot code
                val argsArray = Array<Any>(children.size, {0f})
                var i = 0;
                while (i < children.size) {
                    argsArray[i] = children[i].call(ctx)!!
                    i++
                }
                func.call(*argsArray)!!
            }, func.parameters.map { it.type }, typeOf<R>(), isPure = isPure)
        }

        /**
         * Create a tree node factory that fetch item from the call context.
         * This is basically the tree terminal that gets input.
         */
        inline fun <reified R> fromArgs(argIdx: Int, returnType: KType = typeOf<R>()): FromFuncTreeNodeFactory<R> {
            return FromFuncTreeNodeFactory({ ctx, children ->
                ctx.args[argIdx] as R
            }, listOf(), returnType, isPure = false)
        }

        /**
         * Create a tree node factory that always return the same result.
         */
        inline fun <reified R> fromConstant(constant: R): FromFuncTreeNodeFactory<R> {
            return FromFuncTreeNodeFactory({ ctx, children ->
                constant
            }, listOf(), typeOf<R>())
        }
    }

    /**
     * Some sugar to create another factory with a custom tree shaker
     */
    fun withOptimizer(treeOptimizer: TreeOptimizer<R>): FromFuncTreeNodeFactory<R> {
        val baseOptimizer = this.treeOptimizer
        return FromFuncTreeNodeFactory(func, args, returnType, { treeOptimizer(baseOptimizer(it)) })
    }

    override fun createNode(children: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        return TreeNode(func, returnType, children.toList(), treeOptimizer, isPure)
    }

    override fun canSerialize(tree: BaseTreeNode<*>): Boolean {
        return tree is TreeNode && tree.func == this.func
    }

    override fun serialize(tree: BaseTreeNode<*>, objectMapper: ObjectMapper): JsonNode {
        return objectMapper.createObjectNode()
    }

    override fun deserialize(nodeInfo: JsonNode, children: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        return createNode(children.toList())
    }

    class TreeNode <R>(
        val func: (CallCtx, Array<BaseTreeNode<*>>) -> R,
        override val returnType: KType,
        override val children: List<BaseTreeNode<*>>,
        private val treeOptimizer: TreeOptimizer<R> = { it },
        override val isPure: Boolean = true
    ) : BaseTreeNode<R>() {

        var childArray = children.toTypedArray()

        override fun call(ctx: CallCtx): R {
            return func.invoke(ctx, childArray)
        }

        override fun clone(): BaseTreeNode<R> {
            return TreeNode(func, returnType, children.map { it.clone() }, treeOptimizer, isPure)
        }

        override fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
            return otherTree is TreeNode
                    && otherTree.func == func
        }

        override fun optimizeForEvaluation(): BaseTreeNode<R> {
            return treeOptimizer(this)
        }

        override fun replaceChildren(newChildren: List<BaseTreeNode<*>>): BaseTreeNode<R> {
            return TreeNode(func, returnType, newChildren, treeOptimizer, isPure)
        }
    }
}
