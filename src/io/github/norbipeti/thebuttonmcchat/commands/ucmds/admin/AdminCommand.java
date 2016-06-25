package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

import org.bukkit.command.CommandSender;

public final class AdminCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Admin ----",
				"These commands are for mods only.", "Subcommands: reload, " }; // TODO
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		return false;
	}

	@Override
	public String GetUCommandPath() {
		return "admin";
	}
}
