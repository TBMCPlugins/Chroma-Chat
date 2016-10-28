package buttondevteam.chat.commands;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.chat.TBMCCommandBase;

public final class ChatonlyCommand extends TBMCCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[]{
				"§6---- Chat-only mode ----",
				"This mode makes you invincible but unable to move, teleport or interact with the world in any way",
				"It was designed for chat clients",
				"Once enabled, the only way of disabling it is by relogging to the server"
		};
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		Player player=(Player)sender;
		ChatPlayer p = ChatPlayer.OnlinePlayers.get(player
				.getUniqueId());
		p.ChatOnly = true;
		player.setGameMode(GameMode.SPECTATOR);
		player.sendMessage("§bChat-only mode enabled. You are now invincible.");
		return true;
	}

	@Override
	public String GetCommandPath() {
		return "chatonly";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

	@Override
	public boolean GetModOnly() {
		return false;
	}

}
