package buttondevteam.chat.components.announce;

import org.bukkit.command.CommandSender;

public class RemoveCommand extends AnnounceCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] {
				"§6---- Remove announcement ----",
				"This command removes an announcement",
				"Note: Please avoid using this command, if possible",
				"Instead, use the command blocks in flatworld to set announcements",
				"This makes editing announcements easier" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		if (args.length < 1) {
			sender.sendMessage("§cUsage: /u announce remove <index>");
			return true;
		}
		((AnnouncerComponent) getComponent()).AnnounceMessages().get().remove(Integer.parseInt(args[0]));
		return true;
	}

}
