package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import io.github.norbipeti.thebuttonmcchat.PluginMain;

import org.bukkit.command.CommandSender;

public class SaveCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Save config ----",
				"This command saves the config file(s)" };
	}

	@Override
	public boolean OnAdminCommand(CommandSender sender, String alias,
			String[] args) {
		PluginMain.SaveFiles(); // 2015.08.09.
		sender.sendMessage("§bSaved files. Now you can edit them and reload if you want.§r");
		return true;
	}

	@Override
	public String GetAdminCommandName() {
		return "save";
	}

}
