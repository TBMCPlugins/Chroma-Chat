package buttondevteam.chat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.PluginMain;

public class DebugCommand extends AdminCommandBase {
	public static boolean DebugMode = false;

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Debug mode ----",
				"Toggles debug mode, which prints debug messages to the console." };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		sender.sendMessage("§eDebug mode " + ((DebugMode = !DebugMode) ? "§aenabled." : "§cdisabled."));
		return true;
	}

	public static void SendDebugMessage(String message) {
		if (DebugMode)
			if (PluginMain.Instance != null)
				PluginMain.Instance.getLogger().info(message);
			else
				System.out.println(message);
	}
}
