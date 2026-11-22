package buttondevteam.chat.listener

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.PlayerJoinTimerTask
import buttondevteam.chat.PluginMain
import buttondevteam.chat.commands.ucmds.HistoryCommand
import buttondevteam.chat.components.flair.FlairComponent
import buttondevteam.chat.components.flair.FlairStates
import buttondevteam.core.ComponentManager.isEnabled
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

        if (isEnabled(FlairComponent::class.java)) {
            if (cp.FlairState.get() != FlairStates.NoComment) {
                FlairComponent.Companion.ConfirmUserMessage(cp)
                val timer = Timer()
                val tt: PlayerJoinTimerTask = object : PlayerJoinTimerTask() {
                    public override fun run() {
                        mp.FlairUpdate()
                    }
                }
                tt.mp = cp
                timer.schedule(tt, 1000)
            } //TODO: Better Reddit integration (OAuth)
        }

        var nwithoutformatting: String? = PluginMain.Companion.essentials.getUser(p).getNickname()

        var index: Int
        if (nwithoutformatting != null) {
            while ((nwithoutformatting!!.indexOf("§k").also { index = it }) != -1) nwithoutformatting =
                nwithoutformatting.replace(
                    "§k" + nwithoutformatting.get(index + 2),
                    ""
                ) // Support for one random char

            while ((nwithoutformatting!!.indexOf('§').also { index = it }) != -1) nwithoutformatting =
                nwithoutformatting.replace("§" + nwithoutformatting.get(index + 1), "")
        } else nwithoutformatting = p.getName()
        PlayerListener.Companion.nicknames.forcePut(nwithoutformatting.lowercase(Locale.getDefault()), p.getUniqueId())

        if (PluginMain.Companion.Instance.storeChatHistory.get()) HistoryCommand.Companion.showHistory(
            getFromSender(e.getPlayer()),
            null
        )
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        PlayerListener.Companion.nicknames.inverse().remove(event.getPlayer().getUniqueId())
    }
}
