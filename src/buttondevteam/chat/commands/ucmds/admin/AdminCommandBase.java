package buttondevteam.chat.commands.ucmds.admin;

import buttondevteam.chat.commands.ucmds.UCommandBase;

public abstract class AdminCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias); // TODO: Require
														// permission

	@Override
	public String GetUCommandPath() {
		return "admin/" + GetAdminCommandPath();
	}

	@Override
	public boolean GetPlayerOnly() {
		return false; // Allow admin commands in console
	}

	public abstract String GetAdminCommandPath();

	@Override
	public boolean GetModOnly() {
		return true;
	}
}
