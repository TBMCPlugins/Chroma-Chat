package buttondevteam.chat.commands.ucmds

import buttondevteam.chat.PluginMain
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.player.ChromaGamerBase.InfoTarget
import buttondevteam.lib.player.TBMCPlayer
import buttondevteam.lib.player.TBMCPlayerBase.Companion.getPlayer
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

@CommandClass(
    modOnly = false, helpText = ["User information" //
        , "Get some information known about the user." //
    ]
)
class InfoCommand : UCommandBase() {
    @Subcommand
    fun def(sender: CommandSender, player: OfflinePlayer): Boolean {
        Bukkit.getScheduler().runTaskAsynchronously(PluginMain.Instance!!, Runnable {
            val p = getPlayer(player.uniqueId, TBMCPlayer::class.java)
            sender.sendMessage(p.getInfo(InfoTarget.MCCommand))
        })
        return true
    }
}
