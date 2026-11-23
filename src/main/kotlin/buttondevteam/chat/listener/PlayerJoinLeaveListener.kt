package buttondevteam.chat.listener

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.PluginMain
import buttondevteam.chat.commands.ucmds.HistoryCommand
import buttondevteam.lib.player.ChromaGamerBase.Companion.getFromSender
import buttondevteam.lib.player.TBMCPlayerBase.Companion.getPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class PlayerJoinLeaveListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val p = e.getPlayer()
        val cp = getPlayer<ChatPlayer>(p.getUniqueId(), ChatPlayer::class.java)
        cp.FlairUpdate()

        var nwithoutformatting: String? = PluginMain.essentials!!.getUser(p).nickname

        var index: Int
        if (nwithoutformatting != null) {
            while ((nwithoutformatting!!.indexOf("§k").also { index = it }) != -1) nwithoutformatting =
                nwithoutformatting.replace(
                    "§k" + nwithoutformatting[index + 2],
                    ""
                ) // Support for one random char

            while ((nwithoutformatting!!.indexOf('§').also { index = it }) != -1) nwithoutformatting =
                nwithoutformatting.replace("§" + nwithoutformatting.get(index + 1), "")
        } else nwithoutformatting = p.name
        PlayerListener.nicknames.forcePut(nwithoutformatting.lowercase(Locale.getDefault()), p.uniqueId)

        if (PluginMain.Instance!!.storeChatHistory) HistoryCommand.showHistory(
            getFromSender(e.getPlayer()),
            null
        )
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        PlayerListener.nicknames.inverse().remove(event.getPlayer().uniqueId)
    }
}
