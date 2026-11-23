package buttondevteam.chat.components.formatter.formatting

class FormattedSection internal constructor(
    settings: FormatSettings,
    var Start: Int,
    var End: Int,
    matches: MutableList<String>
) {
    var Settings: FormatSettings = FormatSettings()
    var Matches: MutableList<String> = mutableListOf()

    init {
        Settings.copyFrom(settings)
        Matches.addAll(matches)
    }
}