package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;

import org.bukkit.command.CommandSender;

public final class UCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- U commands ----",
				"Subcommands: help, accept, ignore, admin" };
	}

	@Override
	public boolean OnUCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0)
			return false; // TODO: Forward call to the correct handler
		if (!TBMCCommandBase.GetCommands().containsKey(args[0]))
			return false;
		TBMCCommandBase cmd = TBMCCommandBase.GetCommands().get(args[0]); //Subcommand
	}

	@Override
	public String GetUCommandName() {
		return "u"; // TODO: Same as at AdminCommand
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

}
