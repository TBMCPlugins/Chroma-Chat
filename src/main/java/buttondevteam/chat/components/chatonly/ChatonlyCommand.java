package buttondevteam.chat.components.chatonly;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import buttondevteam.lib.player.TBMCPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@CommandClass(modOnly = false, helpText = {
	"Chat-only mode", //
	"This mode makes you invincible but unable to move, teleport or interact with the world in any way", //
	"It was designed for chat clients", //
	"Once enabled, the only way of disabling it is by relogging to the server" //
})
public final class ChatonlyCommand extends ICommand2MC {

	@Command2.Subcommand
	public boolean def(Player player) {
		ChatPlayer p = TBMCPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class);
		p.ChatOnly = true;
		player.setGameMode(GameMode.SPECTATOR);
		player.sendMessage("§bChat-only mode enabled. You are now invincible.");
		return true;
	}

}
