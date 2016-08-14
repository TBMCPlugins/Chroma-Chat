package buttondevteam.thebuttonmcchat.commands.ucmds.admin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.bukkit.command.CommandSender;

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
		if (args.length == 0)
		{
			sender.sendMessage("§6---- Plugin names ----");
			for(Plugin plugin : TBMC)
			sender.sendMessage("");
		}
		sender.sendMessage("Updating plugin...");
		PluginMain.Instance.getLogger().info("Forced updating of the plugin.");
		URL url;
		try {
			url = new URL("https://github.com/NorbiPeti/thebuttonmcchat/raw/master/TheButtonMCChat.jar");
			FileUtils.copyURLToFile(url, new File("plugins/TheButtonMCChat.jar"));
			sender.sendMessage("Updating done!");
		} catch (MalformedURLException e) {
			PluginMain.Instance.getLogger().warning("Error!\n" + e);
			PluginMain.LastException = e;
		} catch (IOException e) {
			PluginMain.Instance.getLogger().warning("Error!\n" + e);
			PluginMain.LastException = e;
		}
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "updateplugin";
	}

}
