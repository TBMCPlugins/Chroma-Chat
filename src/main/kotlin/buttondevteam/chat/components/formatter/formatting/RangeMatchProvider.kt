package buttondevteam.chat.components.formatter.formatting

import buttondevteam.chat.commands.ucmds.admin.DebugCommand

class RangeMatchProvider(
    name: String,
    private val pattern: String,
    private val settings: FormatSettings
) : MatchProviderBase(name) {
    private var nextIndex = 0
    private var startedSection: FormattedSection? = null

    override fun getNextSection(
        message: String,
        ignoredAreas: MutableList<IntArray>,
        removedCharacters: MutableList<IntArray>
    ): FormattedSection? {
        val i = message.indexOf(pattern, nextIndex)
        val len = pattern.length
        nextIndex = i + len //Set for the next method call
        if (i == -1) {
            isFinished = true //Won't find any more - unfinished sections will be garbage collected
            return null
        }
        if (ChatFormatUtils.isInRange(i, i + len - 1, ignoredAreas)) {
            DebugCommand.SendDebugMessage("Range start is in ignored area, skipping")
            return null //Not setting finished to true, so it will go to the next match
        }
        ignoredAreas.add(intArrayOf(i, i + len - 1))
        if (startedSection == null) {
            DebugCommand.SendDebugMessage("Started range match from " + i + " to " + (i + len - 1))
            DebugCommand.SendDebugMessage("With settings: $settings")
            ChatFormatUtils.sendMessageWithPointer(message, i, i + len - 1)
            startedSection = FormattedSection(settings, i, i + len - 1, mutableListOf())
            return null
        } else {
            val section = startedSection!!
            DebugCommand.SendDebugMessage("Finished range match from " + section.Start + " to " + (i + len - 1))
            DebugCommand.SendDebugMessage("With settings: $settings")
            ChatFormatUtils.sendMessageWithPointer(message, section.Start, i + len - 1)
            section.End = i + len - 1
            removedCharacters.add(intArrayOf(section.Start, section.Start + len - 1))
            removedCharacters.add(intArrayOf(i, i + len - 1))
            startedSection = null //Reset so next find creates a new one
            return section
        }
    }

    public override fun resetSubclass() {
        nextIndex = 0
        startedSection = null
    }
}
