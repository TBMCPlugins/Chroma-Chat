package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;
import io.github.norbipeti.thebuttonmcchat.commands.TBMCSubCommandBase;

public abstract class UCommandBase extends TBMCCommandBase {

	public abstract String[] GetHelpText(String alias);

	@Override
	public String GetCommandPath() {
		return "u/" + GetUCommandPath();
	}

	public abstract String GetUCommandPath(); // TODO: Help for /u commands

	@Override
	public boolean GetPlayerOnly() {
		return true;
	}
}
