package io.github.norbipeti.thebuttonmcchat.commands.ucmds.admin;

import io.github.norbipeti.thebuttonmcchat.PluginMain;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.bukkit.command.CommandSender;

public class UpdatePlugin extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Update plugin ----",
				"This command downloads the latest version of the plugin from GitHub" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		sender.sendMessage("Updating Auto-Flair plugin...");
		System.out.println("Forced updating of Auto-Flair plugin.");
		URL url;
		try {
			url = new URL(
					"https://github.com/NorbiPeti/thebuttonmcchat/raw/master/TheButtonMCChat.jar");
			FileUtils.copyURLToFile(url, new File(
					"plugins/TheButtonMCChat.jar"));
			sender.sendMessage("Updating done!");
		} catch (MalformedURLException e) {
			System.out.println("Error!\n" + e);
			PluginMain.LastException = e;
		} catch (IOException e) {
			System.out.println("Error!\n" + e);
			PluginMain.LastException = e;
		}
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "updateplugin";
	}

}
