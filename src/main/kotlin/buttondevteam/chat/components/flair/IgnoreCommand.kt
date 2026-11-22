package buttondevteam.chat.components.flair

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.commands.ucmds.UCommandBase
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.player.TBMCPlayerBase.Companion.getPlayer
import org.bukkit.entity.Player

@CommandClass(
    modOnly = false, helpText = ["Ignore flair", "Stop the \"write your name in the thread\" message from showing up"
    ]
)
class IgnoreCommand : UCommandBase() {
    @Subcommand
    fun def(player: Player): Boolean {
        val p = getPlayer<ChatPlayer>(player.getUniqueId(), ChatPlayer::class.java)
        if (p.FlairState.get() == FlairStates.Accepted) {
            player.sendMessage("§cYou can only ignore the \"write your name in the thread\" message.")
            return true
        }
        if (p.FlairState.get() == FlairStates.Commented) {
            player.sendMessage("Sorry, but your flair isn't recorded. Please ask a mod to set it for you.")
            return true
        }
        if (p.FlairState.get() != FlairStates.Ignored) {
            p.FlairState.set(FlairStates.Ignored)
            p.SetFlair(ChatPlayer.Companion.FlairTimeNone)
            p.UserName.set("")
            player.sendMessage("§bYou have ignored the message.§r")
        } else player.sendMessage("§cYou already ignored the message.§r")
        return true
    }
}
