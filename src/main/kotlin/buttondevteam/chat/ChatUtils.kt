package buttondevteam.chat

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.ChromaUtils.asString
import buttondevteam.lib.TBMCChatEvent
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.ChromaGamerBase.Companion.getFromSender
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.*

object ChatUtils {
    const val MCORIGIN: String =
        "Minecraft" //Shouldn't change, like ever - TBMCPlayer.getFolderForType(TBMCPlayer.class) capitalized

    /**
     * Dispatch a console command.
     *
     * @param command The command
     * @param async   Whether the caller is async
     */
    fun dispatchConsoleCommand(command: String, async: Boolean) {
        if (async) Bukkit.getScheduler().runTask(
            PluginMain.Instance!!,
            Runnable { Bukkit.dispatchCommand(PluginMain.Console!!, command) })
        else Bukkit.dispatchCommand(PluginMain.Console!!, command)
    }

    /**
     * Returns the string between the start and end strings (exclusive).
     *
     * @param str   The original string
     * @param start The start string
     * @param end   The end string
     * @return The result string
     */
    fun coolSubstring(str: String, start: String, end: String): Optional<String> {
        val a = str.indexOf(start) + start.length
        val b = str.indexOf(end, a)
        return if (a != -1 && b != -1) Optional.of<String>(str.substring(a, b)) else Optional.empty<String>()
    }

    /**
     * Sends a regular (non-Markdown) chat message. Used as a fallback if the chat processing fails.
     *
     * @param e The chat event
     */
    fun sendChatMessage(e: TBMCChatEvent) {
        val str = getMessageString(e.channel, e.user, e.message)
        for (p in Bukkit.getOnlinePlayers()) if (e.shouldSendTo(getFromSender(p))) p.sendMessage(str)
        Bukkit.getConsoleSender().sendMessage(str)
    }

    fun getMessageString(channel: Channel, sender: ChromaGamerBase, message: Component): String {
        return "§c!§r[" + channel.displayName + "] <" + sender.name + "> " + message.asString() // TODO: Components
    }

    fun sendChatMessage(channel: Channel, sender: ChromaGamerBase, message: Component, to: CommandSender) {
        to.sendMessage(getMessageString(channel, sender, message))
    }
}
