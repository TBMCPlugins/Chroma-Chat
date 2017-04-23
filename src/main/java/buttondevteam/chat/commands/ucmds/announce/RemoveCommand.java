package buttondevteam.chat.commands.ucmds.announce;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.PluginMain;

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
		PluginMain.AnnounceMessages.remove(Integer.parseInt(args[0]));
		return true;
	}

}
