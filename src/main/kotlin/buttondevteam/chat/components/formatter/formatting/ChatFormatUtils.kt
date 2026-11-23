package buttondevteam.chat.components.formatter.formatting

import buttondevteam.chat.commands.ucmds.admin.DebugCommand
import java.util.*

object ChatFormatUtils {
    fun sendMessageWithPointer(str: String, vararg pointer: Int) {
        DebugCommand.Companion.SendDebugMessage(str)
        val sb = StringBuilder(str.length)
        Arrays.sort(pointer)
        for (i in pointer.indices) {
            for (j in 0..<pointer[i] - (if (i > 0) pointer[i - 1] + 1 else 0)) sb.append(' ')
            if (pointer[i] == (if (i > 0) pointer[i - 1] else -1)) continue
            sb.append('^')
        }
        DebugCommand.Companion.SendDebugMessage(sb.toString())
    }

    /**
     * Check if the given start and end position is inside any of the ranges
     */
    fun isInRange(start: Int, end: Int, ranges: List<IntArray>): Boolean {
        return ranges.stream().anyMatch { range: IntArray -> range[1] >= start && range[0] <= end }
    }
}
