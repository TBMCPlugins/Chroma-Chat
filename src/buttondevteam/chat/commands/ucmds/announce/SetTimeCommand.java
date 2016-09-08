package buttondevteam.thebuttonmcchat.commands.ucmds.announce;

import org.bukkit.command.CommandSender;

import buttondevteam.thebuttonmcchat.PluginMain;

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
		if (args.length < 3) {
			return false;
		}
		try {
			PluginMain.AnnounceTime = Integer.parseInt(args[0]) * 60 * 1000;
		} catch (Exception e) {
			sender.sendMessage("§cMinutes argument must be a number. Got: "
					+ args[0]);
			return true;
		}
		sender.sendMessage("Time set between announce messages");
		return true;
	}

	@Override
	public String GetAnnounceCommandPath() {
		return "settime";
	}

}
