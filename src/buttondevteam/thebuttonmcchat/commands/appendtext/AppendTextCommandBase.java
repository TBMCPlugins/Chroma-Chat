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
		for (int i = args.length - 1; i >= 0; i--)
			msg = args[i] + " " + msg;
		ChatProcessing.ProcessChat(sender, msg);
		return true;
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

}
