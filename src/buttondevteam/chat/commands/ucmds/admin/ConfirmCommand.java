package buttondevteam.chat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;

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
					ChatPlayer.GetFromPlayer(p);
				if (sender != PluginMain.Console)
					PluginMain.Console.sendMessage("§6-- Reloading done!§r");
				sender.sendMessage("§6-- Reloading done!§r");
				ReloadCommand.Reloader = null;
			} catch (Exception e) {
				PluginMain.Instance.getLogger().warning("Error!\n" + e);
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
