package buttondevteam.chat.components.flair

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase
import buttondevteam.lib.chat.Command2.OptionalArg
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.player.TBMCPlayerBase.Companion.getPlayer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandClass(
    helpText = ["Set flair", "Set a flair for a player", "Usage: /u admin setflair <player> <flairtime (or non-presser, cant-press, none)> <cheater(true/false)> [username]", "Example 1: /u admin setflair NorbiPeti 19 false NorbiPeti --> orange (19s)", "Example 2: /u admin setflair iie 0 true asde --> purple (0s)"
    ]
)
class SetFlairCommand : AdminCommandBase() {
    @Subcommand
    fun def(
        sender: CommandSender,
        player: String,
        flairtime: String,
        cheater: Boolean,
        @OptionalArg username: String?
    ): Boolean {
        val p = Bukkit.getPlayer(player)
        if (p == null) {
            sender.sendMessage("§cPlayer not found.&r")
            return true
        }
        val ft: Short
        if (flairtime.equals("non-presser", ignoreCase = true)) ft = ChatPlayer.Companion.FlairTimeNonPresser.toShort()
        else if (flairtime.equals("cant-press", ignoreCase = true)) ft =
            ChatPlayer.Companion.FlairTimeCantPress.toShort()
        else if (flairtime.equals("none", ignoreCase = true)) ft = ChatPlayer.Companion.FlairTimeNone.toShort()
        else {
            try {
                ft = flairtime.toShort()
            } catch (e: Exception) {
                sender.sendMessage(
                    "§cFlairtime must be a number, \"non-presser\", \"cant-press\" or \"none\". Run without args to see usage."
                )
                return true
            }
        }
        val mp = getPlayer<ChatPlayer>(p.getUniqueId(), ChatPlayer::class.java)
        mp.SetFlair(ft.toInt(), cheater)
        mp.FlairState.set(FlairStates.Accepted)
        if (username == null) mp.UserName.set("")
        else {
            mp.UserName.set(username)
            if (!mp.UserNames.get().contains(username)) mp.UserNames.get().add(username)
        }
        sender.sendMessage("§bThe flair has been set. Player: " + mp.playerName + " Flair: " + mp.GetFormattedFlair() + "§r")
        return true
    }
}
