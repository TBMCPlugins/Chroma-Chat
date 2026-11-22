package buttondevteam.chat.components.formatter

import buttondevteam.chat.PluginMain
import buttondevteam.core.ComponentManager.getIfEnabled
import buttondevteam.core.MainPlugin.Companion.instance
import buttondevteam.core.MainPlugin.isChatHandlerEnabled
import buttondevteam.lib.TBMCChatEvent
import buttondevteam.lib.architecture.Component

/**
 * This component handles the custom processing of chat messages. If this component is disabled channels won't be supported in Minecraft.
 * If you only want to disable the formatting features, set allowFormatting to false.
 * If you're using another chat plugin, you should disable the whole component but that will make it impossible to use channels.
 */
class FormatterComponent : Component<PluginMain?>() {
    /**
     * Determines whether Markdown formatting, name mentioning and similar features are enabled.
     */
    var allowFormatting: IConfigData<Boolean?> = config.getData<Boolean?>("allowFormatting", true)

    /**
     * The sound to play when a player is mentioned. Leave empty to use default.
     */
    var notificationSound: IConfigData<String?> = config.getData<String?>("notificationSound", "")

    /**
     * The pitch of the notification sound.
     */
    var notificationPitch: IConfigData<Float?> = config.getData<Float?>("notificationPitch", 1.0f)

    /**
     * The minimum time between messages in milliseconds.
     */
    var minTimeBetweenMessages: IConfigData<Int?> = config.getData<Int?>("minTimeBetweenMessages", 100)

    override fun enable() {
        MainPlugin.Companion.instance.isChatHandlerEnabled =
            false //Disable Core chat handler - if this component is disabled then let it do its job
    }

    override fun disable() {
        MainPlugin.Companion.instance.isChatHandlerEnabled = true
    }

    companion object {
        /**
         * Handles the chat if the component is enabled.
         *
         * @param event The chat event
         * @return Whether the chat message shouldn't be sent for some reason
         */
        fun handleChat(event: TBMCChatEvent): Boolean {
            val component = getIfEnabled<FormatterComponent?>(FormatterComponent::class.java)
            if (component == null) return false
            return ChatProcessing.ProcessChat(event, component)
        }
    }
}
