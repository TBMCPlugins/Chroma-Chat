package buttondevteam.chat.commands.ucmds;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import org.bukkit.command.CommandSender;

@CommandClass(helpText = {
	"Reload",
	"Reloads the config"
}, modOnly = true)
public class ReloadCommand extends UCommandBase {
	@Command2.Subcommand
	public void def(CommandSender sender) {
		if (PluginMain.Instance.tryReloadConfig())
			sender.sendMessage("§bReloaded config");
		else
			sender.sendMessage("§cFailed to reload config.");
	}
}
