package buttondevteam.chat.commands.ucmds.announce;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;

import buttondevteam.chat.PluginMain;

public class EditCommand extends AnnounceCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Edit announcement ----",
				"This command can only be used in a command block.",
				"Usage: /u annonunce edit <index> <text>" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias,
			String[] args) {
		if (!(sender instanceof BlockCommandSender)) {
			sender.sendMessage("§cError: This command can only be used from a command block. You can use add and remove, though it's not recommended.");
			return true;
		}
		if (args.length < 4) {
			return false;
		}
		StringBuilder sb1 = new StringBuilder();
		for (int i1 = 3; i1 < args.length; i1++) {
			sb1.append(args[i1]);
			if (i1 != args.length - 1)
				sb1.append(" ");
		}
		String finalmessage1 = sb1.toString().replace('&', '§');
		int index = Integer.parseInt(args[2]);
		if (index > 100)
			return false;
		while (PluginMain.AnnounceMessages.size() <= index)
			PluginMain.AnnounceMessages.add("");
		PluginMain.AnnounceMessages.set(Integer.parseInt(args[2]),
				finalmessage1);
		sender.sendMessage("Announcement edited.");
		return true;
	}

}
