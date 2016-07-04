package buttondevteam.thebuttonmcchat.commands.ucmds;

import org.bukkit.command.CommandSender;

import buttondevteam.thebuttonmcchat.commands.CommandCaller;
import buttondevteam.thebuttonmcchat.commands.TBMCCommandBase;

public final class UCommand extends TBMCCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return CommandCaller.GetSubCommands(this);
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		return false;
	}

	@Override
	public String GetCommandPath() {
		return "u";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

}
