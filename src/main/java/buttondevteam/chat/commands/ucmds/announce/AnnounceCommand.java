package buttondevteam.chat.commands.ucmds.announce;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.lib.chat.TBMCChatAPI;

public class AnnounceCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return TBMCChatAPI.GetSubCommands(this);
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
