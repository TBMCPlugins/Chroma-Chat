package buttondevteam.chat.commands;

import org.bukkit.command.CommandSender;

public abstract class TBMCCommandBase {

	public TBMCCommandBase() {
	}

	public abstract String[] GetHelpText(String alias);

	public abstract boolean OnCommand(CommandSender sender, String alias, String[] args);

	public abstract String GetCommandPath();

	public abstract boolean GetPlayerOnly();

	public abstract boolean GetModOnly();
}
