package buttondevteam.chat.commands.ucmds;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.ChatPlayer;

public class NameCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] {
				"§6---- Get username ----",
				"This command allows you to see the Reddit username of a player if they have one associated",
				"Usage: /u name <playername>" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0) {
			return false;
		}
		ChatPlayer mp = ChatPlayer.GetFromName(args[0]);
		if (mp == null) {
			sender.sendMessage("§cUnknown user: "
					+ args[0]);
			return true;
		}
		sender.sendMessage("§bUsername of " + args[0] + ": " + mp.UserName);
		return true;
	}

	@Override
	public String GetUCommandPath() {
		return "name";
	}

}
