package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;

import org.bukkit.command.CommandSender;

public final class UCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- U commands ----",
				"Subcommands: help, accept, ignore, admin" }; //TODO
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		return false;
	}

	@Override
	public String GetUCommandPath() {
		return "u"; // TODO: Same as at AdminCommand
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

}
