package io.github.norbipeti.thebuttonmcchat.commands.ucmds.announce;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

public abstract class AnnounceCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias);

	@Override
	public boolean OnUCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0)
			return false;
		return OnAnnounceCommand(sender, alias,
				Arrays.copyOfRange(args, 1, args.length)); //TODO: Only allow OPs and mods to use it
	}

	public abstract boolean OnAnnounceCommand(CommandSender sender,
			String alias, String[] args);

	@Override
	public String GetUCommandName() {
		return "announce";
	}

	public abstract String GetAnnounceCommandName();

}
