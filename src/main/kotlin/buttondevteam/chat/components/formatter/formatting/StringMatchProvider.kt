package buttondevteam.chat.components.formatter.formatting

import lombok.ToString

class StringMatchProvider(
    name: kotlin.String?,
    @field:ToString.Exclude private val settings: FormatSettings?,
    ignoreCase: kotlin.Boolean,
    vararg strings: kotlin.String
) : MatchProviderBase(name) {
    private val strings: kotlin.Array<kotlin.String>
    private var nextIndex = 0
    private val ignoreCase: kotlin.Boolean

    /**
     * Matches the given strings in the order given
     *
     * @param settings The format settings
     * @param strings  The strings to match in the correct order
     */
    init {
        this.strings = strings
        this.ignoreCase = ignoreCase
        if (ignoreCase) {
            for (i in strings.indices) {
                strings[i] = strings[i].lowercase(java.util.Locale.getDefault())
            }
        }
    }

    @Nullable
    public override fun getNextSection(
        message: kotlin.String,
        ignoredAreas: java.util.ArrayList<kotlin.IntArray?>,
        removedCharacters: java.util.ArrayList<kotlin.IntArray?>?
    ): FormattedSection? {
        var message = message
        if (ignoreCase) message = message.lowercase(java.util.Locale.getDefault())
        var i = -1
        var len = 0
        for (string in strings) {
            i = message.indexOf(string, nextIndex)
            len = string.length
            if (i != -1) break
        }
        if (i == -1) {
            finished = true //Won't find any more
            return null
        }
        nextIndex = i + len
        if (ChatFormatUtils.isInRange(i, i + len - 1, ignoredAreas)) {
            DebugCommand.Companion.SendDebugMessage("String is in ignored area, skipping")
            return null //Not setting finished to true, so it will go to the next match
        }
        DebugCommand.Companion.SendDebugMessage("Found string match from " + i + " to " + (i + len - 1))
        DebugCommand.Companion.SendDebugMessage("With settings: " + settings)
        ChatFormatUtils.sendMessageWithPointer(message, i, i + len - 1)
        ignoredAreas.add(kotlin.intArrayOf(i, i + len - 1))
        return FormattedSection(settings, i, i + len - 1, kotlin.collections.mutableListOf<kotlin.String?>())
    }

    public override fun resetSubclass() {
        nextIndex = 0
    }
}
