package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import io.github.norbipeti.thebuttonmcchat.TBMCPlayer;

import org.bukkit.command.CommandSender;

public class PlayerInfoCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] {
				"§6---- Player info ----",
				"Shows some info about the player's flair, Reddit username(s) and other data known by the plugin",
				"Usage: /u admin playerinfo <player>" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		if (args.length == 0) {
			return false;
		}
		TBMCPlayer p = TBMCPlayer.GetFromName(args[0]);
		if (p == null) {
			sender.sendMessage("§cPlayer not found: " + args[0]
					+ " - Currently only online players can be viewed§r");
			return true;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("§6Usernames:");
		for (String username : p.UserNames)
			sb.append(" ").append(username);
		sender.sendMessage(new String[] { "Player name: " + p.PlayerName,
				"User flair: " + p.GetFormattedFlair(),
				"Username: " + p.UserName, "Flair state: " + p.FlairState,
				sb.toString(), "FCount: " + p.FCount, "FDeaths: " + p.FDeaths });
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "playerinfo";
	}

}
