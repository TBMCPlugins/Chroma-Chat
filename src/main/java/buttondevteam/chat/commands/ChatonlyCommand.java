package buttondevteam.chat.commands;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.PlayerCommandBase;
import buttondevteam.lib.player.TBMCPlayer;

@CommandClass(modOnly = false)
public final class ChatonlyCommand extends PlayerCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Chat-only mode ----", //
				"This mode makes you invincible but unable to move, teleport or interact with the world in any way", //
				"It was designed for chat clients", //
				"Once enabled, the only way of disabling it is by relogging to the server" //
		};
	}

	@Override
	public boolean OnCommand(Player player, String alias, String[] args) {
		ChatPlayer p = TBMCPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class);
		p.ChatOnly = true;
		player.setGameMode(GameMode.SPECTATOR);
		player.sendMessage("§bChat-only mode enabled. You are now invincible.");
		return true;
	}

}
