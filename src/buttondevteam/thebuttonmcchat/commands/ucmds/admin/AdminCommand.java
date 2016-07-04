package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import io.github.norbipeti.thebuttonmcchat.commands.CommandCaller;
import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

import org.bukkit.command.CommandSender;

public final class AdminCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return CommandCaller.GetSubCommands(this);
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		return false;
	}

	@Override
	public String GetUCommandPath() {
		return "admin";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}
}
