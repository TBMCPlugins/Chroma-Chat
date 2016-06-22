package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

public abstract class AdminCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias);

	@Override
	public boolean OnUCommand(CommandSender sender, String alias, String[] args) { // TODO:
																					// Only
																					// mods/admins
																					// should
																					// be
																					// able
																					// to
																					// use
																					// these
		if (args.length == 0)
			return false;
		return OnAdminCommand(sender, alias,
				Arrays.copyOfRange(args, 1, args.length));
	}

	public abstract boolean OnAdminCommand(CommandSender sender, String alias,
			String[] args); // TODO: Actually call subcommands

	@Override
	public String GetUCommandName() {
		return "admin";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false; // Allow admin commands in console
	}

	public abstract String GetAdminCommandName();

}
