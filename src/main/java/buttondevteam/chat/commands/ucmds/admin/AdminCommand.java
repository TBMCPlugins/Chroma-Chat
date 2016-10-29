package buttondevteam.chat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.lib.chat.TBMCChatAPI;

public final class AdminCommand extends UCommandBase {

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
