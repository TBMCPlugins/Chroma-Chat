package buttondevteam.chat.components.chatonly

import buttondevteam.chat.ChatPlayer
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.player.TBMCPlayerBase.Companion.getPlayer
import org.bukkit.GameMode
import org.bukkit.entity.Player

@CommandClass(
    modOnly = false, helpText = ["Chat-only mode" //
        , "This mode makes you invincible but unable to move, teleport or interact with the world in any way" //
        , "It was designed for chat clients" //
        , "Once enabled, the only way of disabling it is by relogging to the server" //
    ]
)
class ChatonlyCommand : ICommand2MC() {
    @Subcommand
    fun def(player: Player): Boolean {
        val p = getPlayer<ChatPlayer>(player.getUniqueId(), ChatPlayer::class.java)
        p.ChatOnly = true
        player.setGameMode(GameMode.SPECTATOR)
        player.sendMessage("§bChat-only mode enabled. You are now invincible.")
        return true
    }
}
