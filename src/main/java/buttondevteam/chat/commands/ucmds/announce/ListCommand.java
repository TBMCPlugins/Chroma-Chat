package buttondevteam.chat.commands.ucmds.announce;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.PluginMain;

public class ListCommand extends AnnounceCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- List announcements ----",
				"This command lists the announcements and the time between them" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		sender.sendMessage("§bList of announce messages:§r");
		sender.sendMessage("§bFormat: [index] message§r");
		int i = 0;
		for (String message : PluginMain.AnnounceMessages)
			sender.sendMessage("[" + i++ + "] " + message);
		sender.sendMessage("§bCurrent wait time between announcements: "
				+ PluginMain.AnnounceTime / 60 / 1000 + " minute(s)§r");
		return true;
	}

	@Override
	public String GetAnnounceCommandPath() {
		return "list";
	}

}
