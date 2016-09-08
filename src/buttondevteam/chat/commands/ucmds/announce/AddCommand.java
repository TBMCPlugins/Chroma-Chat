package buttondevteam.chat.commands.ucmds.announce;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.PluginMain;

public class AddCommand extends AnnounceCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] {
				"§6---- Add announcement ----",
				"This command adds a new announcement",
				"Note: Please avoid using this command, if possible",
				"Instead, use the command blocks in flatworld to set announcements",
				"This makes editing announcements easier" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		if (args.length < 1) {
			return false;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			sb.append(args[i]);
			if (i != args.length - 1)
				sb.append(" ");
		}
		String finalmessage = sb.toString().replace('&', '§');
		PluginMain.AnnounceMessages.add(finalmessage);
		sender.sendMessage("§bAnnouncement added. - Plase avoid using this command if possible, see /u announce add without args.§r");
		return true;
	}

	@Override
	public String GetAnnounceCommandPath() {
		return "add";
	}

}
