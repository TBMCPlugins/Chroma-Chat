package buttondevteam.chat.commands.ucmds.admin;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import org.bukkit.command.CommandSender;

@CommandClass(helpText = {
	"Debug mode",
	"Toggles debug mode, which prints debug messages to the console."
})
public class DebugCommand extends AdminCommandBase {
	public static boolean DebugMode = false;

	@Command2.Subcommand
	public boolean def(CommandSender sender) {
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
