package buttondevteam.chat.components.announce;

import org.bukkit.command.CommandSender;

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
		AnnouncerComponent component = (AnnouncerComponent) getComponent();
		for (String message : component.AnnounceMessages().get())
			sender.sendMessage("[" + i++ + "] " + message);
		sender.sendMessage("§bCurrent wait time between announcements: "
			+ component.AnnounceTime().get() / 60 / 1000 + " minute(s)§r");
		return true;
	}

}
