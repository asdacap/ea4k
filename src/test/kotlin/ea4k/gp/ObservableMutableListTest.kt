package ea4k.gp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ObservableMutableListTest {
    @Test
    fun testObserverTriggeredWhenItemChanged() {
        var changed = false

        val list = ObservableMutableList({
            changed = true
        }, listOf(1))

        list[0] = 2

        assertTrue(changed)
    }
}