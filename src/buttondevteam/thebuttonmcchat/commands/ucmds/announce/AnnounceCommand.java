package buttondevteam.thebuttonmcchat.commands.ucmds.announce;

import org.bukkit.command.CommandSender;

import buttondevteam.thebuttonmcchat.commands.CommandCaller;
import buttondevteam.thebuttonmcchat.commands.ucmds.UCommandBase;

public class AnnounceCommand extends UCommandBase {

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
		return "announce";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}
}
