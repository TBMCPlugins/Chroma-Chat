package buttondevteam.chat.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.TBMCPlayer;
import buttondevteam.lib.chat.TBMCCommandBase;

public final class OOCCommand extends TBMCCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Out-of-character message ----",
				"This command will put a [OCC] tag before your message indicating that you are talking out of character",
				"Usage: /" + alias + " <message>" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		Player player = (Player) sender;
		if (args.length == 0) {
			return false;
		} else {
			final ChatPlayer cp = TBMCPlayer.getPlayerAs(player, ChatPlayer.class);
			cp.RPMode = false;
			String message = "";
			for (String arg : args)
				message += arg + " ";
			player.chat(message.substring(0, message.length() - 1));
			cp.RPMode = true;
		}
		return true;
	}

	@Override
	public String GetCommandPath() {
		return "ooc";
	}

	@Override
	public boolean GetPlayerOnly() {
		return true;
	}

	@Override
	public boolean GetModOnly() {
		return false;
	}

}
