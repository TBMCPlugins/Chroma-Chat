package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import org.bukkit.command.CommandSender;

public class OpmeCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- OP me ----", "Totally makes you OP" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		sender.sendMessage("It would be nice, wouldn't it?");
		return true;
	}

	@Override
	public String GetUCommandPath() {
		return "opme";
	}

}
