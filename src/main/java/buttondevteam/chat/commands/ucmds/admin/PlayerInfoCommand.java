package buttondevteam.chat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.player.TBMCPlayerBase;

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
		ChatPlayer p = TBMCPlayerBase.getFromName(args[0], ChatPlayer.class);
		if (p == null) {
			sender.sendMessage("§cPlayer not found: " + args[0] + "§r");
			return true;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("§6Usernames:");
		for (String username : p.UserNames())
			sb.append(" ").append(username);
		sender.sendMessage(new String[] { //
				"Player name: " + p.PlayerName(), //
				"User flair: " + p.GetFormattedFlair(), //
				"Username: " + p.UserName(), //
				"Flair state: " + p.FlairState(), //
				sb.toString(), //
				"FCount: " + p.FCount(), //
				"FDeaths: " + p.FDeaths() //
		});
		return true;
	}

}
