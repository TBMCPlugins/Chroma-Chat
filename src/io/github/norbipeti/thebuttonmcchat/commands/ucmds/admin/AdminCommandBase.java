package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

public abstract class AdminCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias); //TODO: Require permissionű

	@Override
	public String GetUCommandPath() {
		return "admin/" + GetAdminCommandPath();
	}

	@Override
	public boolean GetPlayerOnly() {
		return false; // Allow admin commands in console
	}

	public abstract String GetAdminCommandPath();

}
