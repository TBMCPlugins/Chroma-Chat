package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import io.github.norbipeti.thebuttonmcchat.TBMCPlayer;
import io.github.norbipeti.thebuttonmcchat.PluginMain;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfirmCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6--- Confirm reload ----",
				"Use this after using /u admin reload and /u admin save" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (ReloadCommand.Reloader == sender) {
			try {
				if (sender != PluginMain.Console)
					PluginMain.Console
							.sendMessage("§6-- Reloading The Button Minecraft plugin...§r");
				sender.sendMessage("§6-- Reloading The Button Minecraft plugin...§r");
				PluginMain.LoadFiles(true);
				// TODO: Add players online
				for (Player p : PluginMain.GetPlayers())
					TBMCPlayer.AddPlayerIfNeeded(p.getUniqueId());
				if (sender != PluginMain.Console)
					PluginMain.Console.sendMessage("§6-- Reloading done!§r");
				sender.sendMessage("§6-- Reloading done!§r");
				ReloadCommand.Reloader = null;
			} catch (Exception e) {
				System.out.println("Error!\n" + e);
				if (sender != PluginMain.Console)
					sender.sendMessage("§cAn error occured. See console for details.§r");
				PluginMain.LastException = e;
			}
		} else
			sender.sendMessage("§cYou need to do /u admin reload first.§r");
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "confirm";
	}

}
