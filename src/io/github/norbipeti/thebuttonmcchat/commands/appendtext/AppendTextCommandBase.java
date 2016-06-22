package io.github.norbipeti.thebuttonmcchat.commands.appendtext;

import io.github.norbipeti.thebuttonmcchat.ChatProcessing;
import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;

import org.bukkit.command.CommandSender;

public abstract class AppendTextCommandBase extends TBMCCommandBase {

	public abstract String[] GetHelpText(String alias);

	public abstract String GetAppendedText();

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		String msg = GetAppendedText();
		if (args.length > 0) {
			msg = args[0] + " " + msg;
		} else
			msg = " " + msg;
		ChatProcessing.ProcessChat(sender, msg);
		return true;
	}

	public abstract String GetCommandName();

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

}
