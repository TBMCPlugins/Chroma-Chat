package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import io.github.norbipeti.thebuttonmcchat.commands.CommandCaller;
import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;

import org.bukkit.command.CommandSender;

public final class UCommand extends TBMCCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return CommandCaller.GetSubCommands(this);
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		return false;
	}

	@Override
	public String GetCommandPath() {
		return "u";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

}
