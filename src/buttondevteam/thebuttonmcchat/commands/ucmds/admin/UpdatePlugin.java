package buttondevteam.thebuttonmcchat.commands.ucmds.admin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import buttondevteam.core.TBMCCoreAPI;
import buttondevteam.thebuttonmcchat.PluginMain;

public class UpdatePlugin extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Update plugin ----",
				"This command downloads the latest version of a TBMC plugin from GitHub",
				"To update a plugin: /" + alias + " <plugin>", "To list the plugin names: /" + alias };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("Downloading plugin names...");
			boolean first = true;
			for (String plugin : TBMCCoreAPI.GetPluginNames()) {
				if (first) {
					sender.sendMessage("§6---- Plugin names ----");
					first = false;
				}
				sender.sendMessage("- " + plugin);
			}
			return true;
		} else {
			sender.sendMessage("Updating plugin...");
			String ret = "";
			if ((ret = TBMCCoreAPI.UpdatePlugin(args[0])).length() > 0) {
				sender.sendMessage("Internal error: " + ret);
				return true;
			}
			sender.sendMessage("Updating done!");
			return true;
		}
	}

	@Override
	public String GetAdminCommandPath() {
		return "updateplugin";
	}

}
