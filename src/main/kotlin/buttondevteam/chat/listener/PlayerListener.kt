package buttondevteam.chat.listener

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.ChatUtils
import buttondevteam.chat.PluginMain
import buttondevteam.chat.commands.ucmds.HistoryCommand
import buttondevteam.chat.components.formatter.FormatterComponent
import buttondevteam.chat.components.towncolors.TownColorComponent
import buttondevteam.core.ComponentManager.isEnabled
import buttondevteam.lib.TBMCChatEvent
import buttondevteam.lib.TBMCCoreAPI.SendException
import buttondevteam.lib.player.ChromaGamerBase.Companion.getFromSender
import buttondevteam.lib.player.TBMCPlayerGetInfoEvent
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import io.papermc.paper.event.player.AsyncChatEvent
import net.ess3.api.events.NickChangeEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatTabCompleteEvent
import java.util.*

class PlayerListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event: AsyncChatEvent) {
        if (event.isCancelled) return
        //The custom event is called in the core, but doesn't cancel the MC event
        if (isEnabled(FormatterComponent::class.java))  //If not enabled, then let the other plugins deal with the message
            event.isCancelled = true // The custom event should only be cancelled when muted or similar
    }

    @EventHandler
    fun onTabComplete(e: PlayerChatTabCompleteEvent) {
        val name = e.lastToken
        for ((key, value) in nicknames) {
            if (key.startsWith(name.lowercase(Locale.getDefault())))
                e.tabCompletions.add(
                    PluginMain.essentials!!.getUser(Bukkit.getPlayer(value)).getNick(true)
                ) //Tabcomplete with the correct case
        }
    }

    @EventHandler
    fun onGetInfo(e: TBMCPlayerGetInfoEvent) {
        val cp = e.player?.getAs(ChatPlayer::class.java) ?: return
        e.addInfo("Minecraft name: " + cp.playerName)
        if (cp.UserName.isNotEmpty()) e.addInfo("Reddit name: " + cp.UserName)
        e.addInfo(String.format("Respect: %.2f", cp.f))
    }

    private var lastError: Long = 0

    @EventHandler
    fun onPlayerTBMCChat(e: TBMCChatEvent) {
        try {
            if (e.isCancelled) return
            HistoryCommand.addChatMessage(e.chatMessage, e.channel)
            e.isCancelled = FormatterComponent.handleChat(e)
        } catch (ex: NoClassDefFoundError) { // Weird things can happen
            if (lastError < System.nanoTime() - 1000L * 1000L * 1000L * 60 * 60 //60 mins
                && Bukkit.getOnlinePlayers().isNotEmpty()
            ) { //If there are no players on, display to the first person
                lastError = System.nanoTime() //I put the whole thing in the if to protect against race conditions
                for (p in Bukkit.getOnlinePlayers())
                    if (e.shouldSendTo(getFromSender(p)))
                        p.sendMessage("[" + e.channel.displayName + "] §cSome features in the message below might be unavailable due to an error.")
            }
            ChatUtils.sendChatMessage(e)
            SendException("An error occured while processing a chat message!", ex, PluginMain.Instance!!)
        } catch (ex: Exception) {
            if (lastError < System.nanoTime() - 1000L * 1000L * 1000L * 60 * 60
                && Bukkit.getOnlinePlayers().isNotEmpty()
            ) {
                lastError = System.nanoTime()
                for (p in Bukkit.getOnlinePlayers())
                    if (e.shouldSendTo(getFromSender(p)))
                        p.sendMessage("[" + e.channel.displayName + "] §cSome features in the message below might be unavailable due to an error.")
            }
            ChatUtils.sendChatMessage(e)
            SendException("An error occured while processing a chat message!", ex, PluginMain.Instance!!)
        }
    }

    @EventHandler
    fun onNickChange(e: NickChangeEvent) {
        val nick = e.value
        if (nick == null) nicknames.inverse()
            .remove(e.getController().base.uniqueId) //EssentialsX has it swapped
        else nicknames.inverse().forcePut(
            e.getController().base.uniqueId,
            ChatColor.stripColor(nick)!!.lowercase(Locale.getDefault())
        )

        Bukkit.getScheduler().runTaskLater(PluginMain.Instance!!, Runnable {
            TownColorComponent.updatePlayerColors(e.getAffected().base) //Won't fire this event again
        }, 1)
    }

    companion object {
        /**
         * Does not contain format codes, lowercased
         */
        val nicknames: BiMap<String, UUID> = HashBiMap.create()
    }
}
