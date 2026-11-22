package buttondevteam.chat.components.formatter.formatting

import lombok.ToString

@ToString
class FormattedSection internal constructor(
    settings: FormatSettings?,
    var Start: kotlin.Int,
    var End: kotlin.Int,
    matches: kotlin.collections.MutableList<kotlin.String?>
) {
    var Settings: FormatSettings
    var Matches: kotlin.collections.MutableList<kotlin.String?> = java.util.ArrayList<kotlin.String?>()

    init {
        Settings = builder().build()
        Settings.copyFrom(settings)
        Matches.addAll(matches)
    }
}