package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import io.github.norbipeti.thebuttonmcchat.MaybeOfflinePlayer;

import org.bukkit.command.CommandSender;

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
		if (args.length == 1) {
			return false;
		}
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.GetFromName(args[1]);
		if (mp == null) {
			sender.sendMessage("§cUnknown user (player has to be online): "
					+ args[1]);
			return true;
		}
		sender.sendMessage("§bUsername of " + args[1] + ": " + mp.UserName);
		return true;
	}

	@Override
	public String GetUCommandPath() {
		return "name";
	}

}
