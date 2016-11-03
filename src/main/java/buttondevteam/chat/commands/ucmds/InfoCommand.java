package buttondevteam.chat.commands.ucmds;

import org.bukkit.command.CommandSender;

import buttondevteam.lib.TBMCPlayer;
import buttondevteam.lib.TBMCPlayer.InfoTarget;

public class InfoCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { //
				"§6---- User information ----", //
				"Get some information known about the user.", //
				"Usage: /u " + alias + " <playername>" //
		};
	}

	@Override
	public String GetUCommandPath() {
		return "info";
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0)
			return false;
		if (args[0].equalsIgnoreCase("console") || args[0].equalsIgnoreCase("server")
				|| args[0].equalsIgnoreCase("@console")) {
			sender.sendMessage("The server console."); // TODO: Console login? The Discord thing might replace it
			return true;
		}
		try (TBMCPlayer p = TBMCPlayer.getFromName(args[0])) {
			if (p == null) {
				sender.sendMessage("§cThe specified player cannot be found");
				return true;
			}
			sender.sendMessage(p.getInfo(InfoTarget.MCCommand));
		}
		return true;
	}

}
