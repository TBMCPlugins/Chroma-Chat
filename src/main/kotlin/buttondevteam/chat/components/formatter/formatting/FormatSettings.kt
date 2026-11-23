package buttondevteam.chat.components.formatter.formatting

import net.kyori.adventure.text.format.TextColor

/**
 * Describes how a matched section of the message should look. May be combined with other format settings.
 */
class FormatSettings(
    var italic: Boolean = false, var bold: Boolean = false, var underlined: Boolean = false,
    var strikethrough: Boolean = false, var obfuscated: Boolean = false, var color: TextColor? = null,
    var onmatch: ((String, FormatSettings, FormattedSection) -> String)? = null,
    var openlink: String? = null, var hoverText: String? = null
) {
    fun copyFrom(settings: FormatSettings?) {
        try {
            for (field in FormatSettings::class.java.declaredFields) {
                if (field.type == Boolean::class.javaPrimitiveType) {
                    if (field.getBoolean(settings))
                        field.setBoolean(this, true) //Set to true if either of them are true
                } else if (field.get(settings) != null) {
                    field.set(this, field.get(settings))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
