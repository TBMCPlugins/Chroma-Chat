package buttondevteam.chat.components.formatter.formatting

import lombok.ToString

class RegexMatchProvider(
    name: kotlin.String?,
    private val pattern: java.util.regex.Pattern,
    @field:ToString.Exclude private val settings: FormatSettings?
) : MatchProviderBase(name) {
    private var matcher: java.util.regex.Matcher? = null

    @Nullable
    public override fun getNextSection(
        message: kotlin.String,
        ignoredAreas: java.util.ArrayList<kotlin.IntArray?>,
        removedCharacters: java.util.ArrayList<kotlin.IntArray?>?
    ): FormattedSection? {
        if (matcher == null) matcher = pattern.matcher(message)
        if (!matcher!!.find()) {
            finished = true
            return null
        }
        val start = matcher!!.start()
        val end = matcher!!.end() - 1
        DebugCommand.Companion.SendDebugMessage("Found regex match from " + start + " to " + end)
        DebugCommand.Companion.SendDebugMessage("With settings: " + settings)
        ChatFormatUtils.sendMessageWithPointer(message, start, end)
        if (ChatFormatUtils.isInRange(start, end, ignoredAreas)) {
            DebugCommand.Companion.SendDebugMessage("Match is in ignored area, skipping")
            return null //Not setting finished to true, so it will go to the next match
        }
        val groups = java.util.ArrayList<kotlin.String?>()
        for (i in 0..<matcher!!.groupCount()) groups.add(matcher!!.group(i + 1))
        if (groups.size > 0) DebugCommand.Companion.SendDebugMessage("First group: " + groups.get(0))
        ignoredAreas.add(kotlin.intArrayOf(start, end))
        return FormattedSection(settings, matcher!!.start(), matcher!!.end() - 1, groups)
    }

    public override fun resetSubclass() {
        matcher = null
    }
}
