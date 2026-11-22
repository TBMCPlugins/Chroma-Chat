package buttondevteam.chat.components.formatter.formatting

import buttondevteam.lib.chat.TellrawSerializableEnum

class TellrawEvent<T : buttondevteam.chat.components.formatter.formatting.TellrawEvent.Action?> : java.io.Serializable {
    @kotlin.jvm.Transient
    val isHoverEvent: kotlin.Boolean
    val action: T?
    val value: kotlin.Any?

    private constructor(action: T?, value: kotlin.String?) {
        this.isHoverEvent = action is HoverAction
        this.action = action
        this.value = value
    }

    private constructor(action: T?, value: TellrawPart?) {
        this.isHoverEvent = action is HoverAction
        this.action = action
        this.value = value
    }

    enum class ClickAction(val name: kotlin.String) :
        buttondevteam.chat.components.formatter.formatting.TellrawEvent.Action {
        OPEN_URL("open_url"), RUN_COMMAND("run_command"), SUGGEST_COMMAND("suggest_command")
    }

    enum class HoverAction(val name: kotlin.String) :
        buttondevteam.chat.components.formatter.formatting.TellrawEvent.Action {
        SHOW_TEXT("show_text"), SHOW_ITEM("show_item"), SHOW_ACHIEVEMENT("show_achievement"), SHOW_ENTITY(
            "show_entity"
        )
    }

    interface Action : TellrawSerializableEnum
    companion object {
        private val serialVersionUID = -1681364161210561505L
        fun <V : buttondevteam.chat.components.formatter.formatting.TellrawEvent.Action?> create(
            action: V?,
            value: kotlin.String?
        ): TellrawEvent<V?> {
            return TellrawEvent<V?>(action, value)
        }

        fun <V : buttondevteam.chat.components.formatter.formatting.TellrawEvent.Action?> create(
            action: V?,
            value: TellrawPart?
        ): TellrawEvent<V?> {
            return TellrawEvent<V?>(action, value)
        }
    }
}
