package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;

public abstract class UCommandBase extends TBMCCommandBase {

	public abstract String[] GetHelpText(String alias);

	@Override
	public String GetCommandPath() {
		if (GetUCommandPath().equals("u"))
			return "u";
		return "u/" + GetUCommandPath(); //TODO: This for others
	}

	public abstract String GetUCommandPath(); // TODO: Help for /u commands

	@Override
	public boolean GetPlayerOnly() {
		return true;
	}
}
