package io.github.norbipeti.thebuttonmcchat.commands.ucmds.announce;

import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

import org.bukkit.command.CommandSender;

public class AnnounceCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Announce ----",
				"Subcommands: add, settime, remove, list, edit" }; // TODO
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		return false;
	}

	@Override
	public String GetUCommandPath() {
		return "announce";
	}

}
