package buttondevteam.chat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.TBMCPlayer;

public class PlayerInfoCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Player info ----",
				"Shows some info about the player's flair, Reddit username(s) and other data known by the plugin",
				"Usage: /u admin playerinfo <player>" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0) {
			return false;
		}
		ChatPlayer p = TBMCPlayer.getFromName(args[0]).asPluginPlayer(ChatPlayer.class);
		if (p == null) {
			sender.sendMessage("§cPlayer not found: " + args[0] + "§r");
			return true;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("§6Usernames:");
		for (String username : p.getUserNames())
			sb.append(" ").append(username);
		sender.sendMessage(new String[] { //
				"Player name: " + p.getPlayerName(), //
				"User flair: " + p.GetFormattedFlair(), //
				"Username: " + p.getUserName(), //
				"Flair state: " + p.getFlairState(), //
				sb.toString(), //
				"FCount: " + p.getFCount(), //
				"FDeaths: " + p.getFDeaths() //
		});
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "playerinfo";
	}

}
