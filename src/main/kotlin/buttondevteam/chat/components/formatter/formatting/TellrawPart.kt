package buttondevteam.chat.components.formatter.formatting

import buttondevteam.lib.chat.Color

class TellrawPart(var text: kotlin.String?) : java.io.Serializable {
    private var color: Color? = null
    var isItalic: kotlin.Boolean = false
        private set
    var isBold: kotlin.Boolean = false
        private set
    var isUnderlined: kotlin.Boolean = false
        private set
    var isStrikethrough: kotlin.Boolean = false
        private set
    var isObfuscated: kotlin.Boolean = false
        private set
    private val extra: kotlin.collections.MutableList<TellrawPart?> = java.util.ArrayList<TellrawPart?>()
    private var hoverEvent: TellrawEvent<HoverAction?>? = null
    private var clickEvent: TellrawEvent<ClickAction?>? = null

    fun getColor(): Color? {
        return color
    }

    fun setColor(color: Color?): TellrawPart {
        this.color = color
        return this
    }

    fun setItalic(italic: kotlin.Boolean): TellrawPart {
        this.isItalic = italic
        return this
    }

    fun setBold(bold: kotlin.Boolean): TellrawPart {
        this.isBold = bold
        return this
    }

    fun setUnderlined(underlined: kotlin.Boolean): TellrawPart {
        this.isUnderlined = underlined
        return this
    }

    fun setStrikethrough(strikethrough: kotlin.Boolean): TellrawPart {
        this.isStrikethrough = strikethrough
        return this
    }

    fun setObfuscated(obfuscated: kotlin.Boolean): TellrawPart {
        this.isObfuscated = obfuscated
        return this
    }

    fun getExtra(): kotlin.collections.Iterable<TellrawPart?> {
        return extra
    }

    fun addExtra(extra: TellrawPart?): TellrawPart {
        this.extra.add(extra)
        return this
    }

    fun setText(text: kotlin.String?): TellrawPart {
        this.text = text
        return this
    }

    fun getHoverEvent(): TellrawEvent<HoverAction?>? {
        return hoverEvent
    }

    fun setHoverEvent(hoverEvent: TellrawEvent<HoverAction?>?): TellrawPart {
        this.hoverEvent = hoverEvent
        return this
    }

    fun getClickEvent(): TellrawEvent<ClickAction?>? {
        return clickEvent
    }

    fun setClickEvent(clickEvent: TellrawEvent<ClickAction?>?): TellrawPart {
        this.clickEvent = clickEvent
        return this
    }

    companion object {
        private const val serialVersionUID = 4125357644462144024L
    }
}
