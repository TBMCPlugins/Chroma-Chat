package buttondevteam.chat.commands.ucmds.admin;

import org.bukkit.command.CommandSender;

public class TCCount extends AdminCommandBase {
	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { //
				"§6---- Town Color Count", //
				"Sets how many colors can be used for a town." //
		};
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		byte count;
		try {
			if (args.length == 0 || (count = Byte.parseByte(args[0])) <= 0)
				return false;
		} catch (NumberFormatException e) {
			return false;
		}
		buttondevteam.chat.commands.ucmds.TownColorCommand.ColorCount = count;
		sender.sendMessage("Color count set to " + count);
		return true;
	}
}
