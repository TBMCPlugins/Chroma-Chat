package buttondevteam.thebuttonmcchat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

import buttondevteam.thebuttonmcchat.commands.CommandCaller;
import buttondevteam.thebuttonmcchat.commands.ucmds.UCommandBase;

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

	@Override
	public boolean GetModOnly() {
		return true;
	}
}
