package buttondevteam.chat.components.announce

import buttondevteam.chat.PluginMain
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget
import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget.Companion.add
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.ComponentMetadata
import buttondevteam.lib.chat.TBMCChatAPI.SendSystemMessage
import org.bukkit.Bukkit

/**
 * Displays the configured messages at the set interval when someone is online.
 */
@ComponentMetadata(enabledByDefault = false)
class AnnouncerComponent : Component<PluginMain>(), Runnable {
    /**
     * The messages to display to players.
     */
    var announceMessages by config.getListData("announceMessages", mutableListOf<String>())

    /**
     * The time in milliseconds between the messages. Use /u announce settime to set minutes.
     */
    var announceTime by config.getData("announceTime", 15 * 60 * 1000)

    private var target: BroadcastTarget? = null

    private var AnnounceMessageIndex = 0

    override fun run() {
        while (isEnabled) {
            try {
                Thread.sleep(announceTime.toLong()) // TODO: Coroutines
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            if (Bukkit.getOnlinePlayers().isEmpty()) continue  //Don't post to Discord if nobody is on

            if (announceMessages.size > AnnounceMessageIndex) {
                SendSystemMessage(
                    Channel.globalChat,
                    Channel.RecipientTestResult.ALL,
                    announceMessages[AnnounceMessageIndex],
                    target
                )
                AnnounceMessageIndex++
                if (AnnounceMessageIndex == announceMessages.size) AnnounceMessageIndex = 0
            }
        }
    }

    override fun enable() {
        target = add("announcements")
        registerCommand(AnnounceCommand(this))
        Thread(this).start()
    }

    override fun disable() {
    }
}
