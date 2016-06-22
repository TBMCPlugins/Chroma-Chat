package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;

public abstract class UCommandBase extends TBMCCommandBase {

	public abstract String[] GetHelpText(String alias);

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		return OnUCommand(sender, alias,
				Arrays.copyOfRange(args, 1, args.length));
	}

	public abstract boolean OnUCommand(CommandSender sender, String alias,
			String[] args);

	@Override
	public String GetCommandName() {
		return "u";
	}

	public abstract String GetUCommandName(); //TODO: Help for /u commands

	@Override
	public boolean GetPlayerOnly() {
		return true;
	}

}
