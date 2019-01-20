package buttondevteam.chat.components.announce;

import org.bukkit.command.CommandSender;

public class SetTimeCommand extends AnnounceCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Set time ----",
				"This command sets the time between the announcements",
				"Usage: /u anonunce settime <minutes>", "Default: 15" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		if (args.length < 1) {
			return false;
		}
		try {
			((AnnouncerComponent) getComponent()).AnnounceTime().set(Integer.parseInt(args[0]) * 60 * 1000);
		} catch (Exception e) {
			sender.sendMessage("§cMinutes argument must be a number. Got: "
					+ args[0]);
			return true;
		}
		sender.sendMessage("Time set between announce messages");
		return true;
	}

}
