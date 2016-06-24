package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

public abstract class AdminCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias);

	@Override
	public String GetUCommandPath() {
		if (GetAdminCommandName().equals("admin"))
			return "admin";
		return "admin/" + GetAdminCommandName();
	}

	@Override
	public boolean GetPlayerOnly() {
		return false; // Allow admin commands in console
	}

	public abstract String GetAdminCommandName();

}
