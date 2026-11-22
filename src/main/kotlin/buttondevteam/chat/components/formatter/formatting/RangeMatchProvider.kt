package buttondevteam.chat.components.formatter.formatting

import lombok.ToString

class RangeMatchProvider(
    name: kotlin.String?,
    private val pattern: kotlin.String,
    @field:ToString.Exclude private val settings: FormatSettings?
) : MatchProviderBase(name) {
    private var nextIndex = 0
    private var startedSection: FormattedSection? = null

    public override fun getNextSection(
        message: kotlin.String,
        ignoredAreas: java.util.ArrayList<kotlin.IntArray?>,
        removedCharacters: java.util.ArrayList<kotlin.IntArray?>
    ): FormattedSection? {
        val i: kotlin.Int
        val len: kotlin.Int
        i = message.indexOf(pattern, nextIndex)
        len = pattern.length
        nextIndex = i + len //Set for the next method call
        if (i == -1) {
            finished = true //Won't find any more - unfinished sections will be garbage collected
            return null
        }
        if (ChatFormatUtils.isInRange(i, i + len - 1, ignoredAreas)) {
            DebugCommand.Companion.SendDebugMessage("Range start is in ignored area, skipping")
            return null //Not setting finished to true, so it will go to the next match
        }
        ignoredAreas.add(kotlin.intArrayOf(i, i + len - 1))
        if (startedSection == null) {
            DebugCommand.Companion.SendDebugMessage("Started range match from " + i + " to " + (i + len - 1))
            DebugCommand.Companion.SendDebugMessage("With settings: " + settings)
            ChatFormatUtils.sendMessageWithPointer(message, i, i + len - 1)
            startedSection =
                FormattedSection(settings, i, i + len - 1, kotlin.collections.mutableListOf<kotlin.String?>())
            return null
        } else {
            val section = startedSection!!
            DebugCommand.Companion.SendDebugMessage("Finished range match from " + section.Start + " to " + (i + len - 1))
            DebugCommand.Companion.SendDebugMessage("With settings: " + settings)
            ChatFormatUtils.sendMessageWithPointer(message, section.Start, i + len - 1)
            section.End = i + len - 1
            removedCharacters.add(kotlin.intArrayOf(section.Start, section.Start + len - 1))
            removedCharacters.add(kotlin.intArrayOf(i, i + len - 1))
            startedSection = null //Reset so next find creates a new one
            return section
        }
    }

    public override fun resetSubclass() {
        nextIndex = 0
        startedSection = null
    }
}
