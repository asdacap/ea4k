package ea4k.gp

import kotlin.reflect.KType

class RootTreeNode<R>(child: BaseTreeNode<R>) : BaseTreeNode<R>() {
    override val children: MutableList<BaseTreeNode<*>> = mutableListOf(child)
    override val returnType: KType = children[0].returnType

    override fun call(ctx: CallCtx): R {
        return children[0].call(ctx) as R
    }

    override fun isNodeEffectivelySame(otherTree: BaseTreeNode<*>): Boolean {
        return otherTree is RootTreeNode && children[0].isNodeEffectivelySame(otherTree.children[0]) ||
                otherTree.children[0].isNodeEffectivelySame(otherTree)
    }

    override fun clone(): BaseTreeNode<R> {
        return RootTreeNode(children[0].clone()) as BaseTreeNode<R>
    }

    override fun optimizeForEvaluation(): BaseTreeNode<R> {
        return children[0] as BaseTreeNode<R>
    }

    override fun replaceChildren(newChildren: List<BaseTreeNode<*>>): BaseTreeNode<R> {
        return RootTreeNode(newChildren[0]) as BaseTreeNode<R>
    }
}