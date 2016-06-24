package io.github.norbipeti.thebuttonmcchat.commands;

import io.github.norbipeti.thebuttonmcchat.MaybeOfflinePlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class OOCCommand extends TBMCCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] {
				"§6---- Out-of-character message ----",
				"This command will put a [OCC] tag before your message indicating that you are talking out of character",
				"Usage: /" + alias + " <message>" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		Player player = (Player) sender;
		if (args.length == 0) {
			return false;
		} else {
			MaybeOfflinePlayer.AddPlayerIfNeeded(player.getUniqueId()).RPMode = false;
			String message = "";
			for (String arg : args)
				message += arg + " ";
			player.chat(message.substring(0, message.length() - 1));
			MaybeOfflinePlayer.AddPlayerIfNeeded(player.getUniqueId()).RPMode = true;
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

}
