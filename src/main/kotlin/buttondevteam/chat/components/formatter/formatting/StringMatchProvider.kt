package buttondevteam.chat.components.formatter.formatting

import buttondevteam.chat.commands.ucmds.admin.DebugCommand
import java.util.*

class StringMatchProvider(
    name: String,
    private val settings: FormatSettings,
    private val ignoreCase: Boolean,
    vararg strings: String
) : MatchProviderBase(name) {
    private var nextIndex = 0
    private val strings: Array<String>

    init {
        if (ignoreCase) {
            this.strings = strings.map { it.lowercase() }.toTypedArray()
        } else {
            this.strings = strings.map { it }.toTypedArray()
        }
    }

    override fun getNextSection(
        message: String,
        ignoredAreas: MutableList<IntArray>,
        removedCharacters: MutableList<IntArray>
    ): FormattedSection? {
        var message = message
        if (ignoreCase) message = message.lowercase(Locale.getDefault())
        var i = -1
        var len = 0
        for (string in strings) {
            i = message.indexOf(string, nextIndex)
            len = string.length
            if (i != -1) break
        }
        if (i == -1) {
            isFinished = true //Won't find any more
            return null
        }
        nextIndex = i + len
        if (ChatFormatUtils.isInRange(i, i + len - 1, ignoredAreas)) {
            DebugCommand.SendDebugMessage("String is in ignored area, skipping")
            return null //Not setting finished to true, so it will go to the next match
        }
        DebugCommand.SendDebugMessage("Found string match from " + i + " to " + (i + len - 1))
        DebugCommand.SendDebugMessage("With settings: $settings")
        ChatFormatUtils.sendMessageWithPointer(message, i, i + len - 1)
        ignoredAreas.add(intArrayOf(i, i + len - 1))
        return FormattedSection(settings, i, i + len - 1, mutableListOf())
    }

    public override fun resetSubclass() {
        nextIndex = 0
    }
}
