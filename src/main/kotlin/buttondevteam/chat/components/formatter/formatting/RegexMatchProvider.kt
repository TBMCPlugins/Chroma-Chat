package buttondevteam.chat.components.formatter.formatting

import buttondevteam.chat.commands.ucmds.admin.DebugCommand
import java.util.regex.Pattern

class RegexMatchProvider(
    name: String,
    private val pattern: Pattern,
    private val settings: FormatSettings
) : MatchProviderBase(name) {
    private var matcher: java.util.regex.Matcher? = null

    override fun getNextSection(
        message: String,
        ignoredAreas: MutableList<IntArray>,
        removedCharacters: MutableList<IntArray>
    ): FormattedSection? {
        if (matcher == null) matcher = pattern.matcher(message)
        if (!matcher!!.find()) {
            isFinished = true
            return null
        }
        val start = matcher!!.start()
        val end = matcher!!.end() - 1
        DebugCommand.SendDebugMessage("Found regex match from $start to $end")
        DebugCommand.SendDebugMessage("With settings: $settings")
        ChatFormatUtils.sendMessageWithPointer(message, start, end)
        if (ChatFormatUtils.isInRange(start, end, ignoredAreas)) {
            DebugCommand.SendDebugMessage("Match is in ignored area, skipping")
            return null //Not setting finished to true, so it will go to the next match
        }
        val groups = mutableListOf<String>()
        for (i in 0..<matcher!!.groupCount()) groups.add(matcher!!.group(i + 1))
        if (groups.isNotEmpty()) DebugCommand.SendDebugMessage("First group: " + groups[0])
        ignoredAreas.add(intArrayOf(start, end))
        return FormattedSection(settings, matcher!!.start(), matcher!!.end() - 1, groups)
    }

    public override fun resetSubclass() {
        matcher = null
    }
}
