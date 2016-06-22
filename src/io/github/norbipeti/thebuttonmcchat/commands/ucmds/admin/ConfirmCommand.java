package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import io.github.norbipeti.thebuttonmcchat.PluginMain;

import org.bukkit.command.CommandSender;

public class ConfirmCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6--- Confirm reload ----",
				"Use this after using /u admin reload and /u admin save" };
	}

	@Override
	public boolean OnAdminCommand(CommandSender sender, String alias,
			String[] args) {
		if (ReloadCommand.Reloader == sender) {
			try {
				if (sender != PluginMain.Console)
					PluginMain.Console
							.sendMessage("§6-- Reloading The Button Minecraft plugin...§r");
				sender.sendMessage("§6-- Reloading The Button Minecraft plugin...§r");
				PluginMain.LoadFiles(true);
				if (sender != PluginMain.Console)
					PluginMain.Console.sendMessage("§6-- Reloading done!§r");
				sender.sendMessage("§6-- Reloading done!§r");
			} catch (Exception e) {
				System.out.println("Error!\n" + e);
				if (sender != PluginMain.Console)
					sender.sendMessage("§cAn error occured. See console for details.§r");
				PluginMain.LastException = e; // 2015.08.09.
			}
		} else
			sender.sendMessage("§cYou need to do /u admin reload first.§r");
		return true;
	}

	@Override
	public String GetAdminCommandName() {
		return "confirm";
	}

}
