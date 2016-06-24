package io.github.norbipeti.thebuttonmcchat.commands;

import java.util.HashMap;

import io.github.norbipeti.thebuttonmcchat.PluginMain;
import io.github.norbipeti.thebuttonmcchat.commands.appendtext.ShrugCommand;
import io.github.norbipeti.thebuttonmcchat.commands.appendtext.TableflipCommand;
import io.github.norbipeti.thebuttonmcchat.commands.appendtext.UnflipCommand;
import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class TBMCCommandBase {

	public TBMCCommandBase() {
	}

	public abstract String[] GetHelpText(String alias);

	public abstract boolean OnCommand(CommandSender sender, String alias,
			String[] args);

	public abstract String GetCommandPath();

	public abstract boolean GetPlayerOnly();
}
