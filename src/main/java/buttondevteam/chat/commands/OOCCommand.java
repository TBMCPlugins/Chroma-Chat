package buttondevteam.chat.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.PlayerCommandBase;
import buttondevteam.lib.chat.TBMCCommandBase;
import buttondevteam.lib.player.TBMCPlayer;

@CommandClass(modOnly = false)
public final class OOCCommand extends PlayerCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Out-of-character message ----",
				"This command will put a [OCC] tag before your message indicating that you are talking out of character",
				"Usage: /" + alias + " <message>" };
	}

	@Override
	public boolean OnCommand(Player player, String alias, String[] args) {
		if (args.length == 0) {
			return false;
		} else {
			final ChatPlayer cp = TBMCPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class);
			cp.RPMode = false;
			String message = "";
			for (String arg : args)
				message += arg + " ";
			player.chat(message.substring(0, message.length() - 1));
			cp.RPMode = true;
		}
		return true;
	}

}
