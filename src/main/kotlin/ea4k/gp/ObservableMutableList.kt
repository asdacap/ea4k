package ea4k.gp

/**
 * Utility class to create a MutableList with an onChange handler.
 * Used by TreeNode which may need to change its internal structure when its children changed
 */
class ObservableMutableList<T>(
    val onChange: () -> Unit,
    val originalItems: List<T> = listOf(),
    private val arrayList: ArrayList<T> = ArrayList<T>(originalItems)
): MutableList<T> by arrayList {
    override fun set(index: Int, element: T): T {
        val ret = arrayList.set(index, element)
        onChange()
        return ret
    }
}