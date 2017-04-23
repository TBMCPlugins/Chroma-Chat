package buttondevteam.chat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.PluginMain;

public class SaveCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Save config ----",
				"This command saves the config file(s)" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		PluginMain.SaveFiles(); // 2015.08.09.
		sender.sendMessage("§bSaved files. Now you can edit them and reload if you want.§r");
		return true;
	}

}
