package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

public final class AdminCommand extends AdminCommandBase {

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
	public String GetAdminCommandName() {
		return "admin"; //TODO: Call this by default (so /u admin invalidcmd should point here)
	}
}
