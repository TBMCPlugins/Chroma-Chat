package io.github.norbipeti.thebuttonmcchat.commands.ucmds.announce;

import org.bukkit.command.CommandSender;

public class AnnounceCommand extends AnnounceCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Announce ----",
				"Subcommands: add, settime, remove, list, edit" }; // TODO
	}

	@Override
	public boolean OnAnnounceCommand(CommandSender sender, String alias,
			String[] args) {
		return false;
	}

	@Override
	public String GetAnnounceCommandName() {
		return "announce";
	}

}
