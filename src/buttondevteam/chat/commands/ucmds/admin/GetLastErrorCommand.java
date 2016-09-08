package buttondevteam.thebuttonmcchat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

import buttondevteam.thebuttonmcchat.PluginMain;

public class GetLastErrorCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Get last error ----",
				"This command returns the last exception",
				"Note that not all exceptions are recorded" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		if (PluginMain.LastException != null) {
			sender.sendMessage("Last error:");
			sender.sendMessage(PluginMain.LastException.toString());
			PluginMain.LastException = null;
		} else
			sender.sendMessage("There were no exceptions.");
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "getlasterror";
	}

}
