package io.github.norbipeti.thebuttonmcchat.commands;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.norbipeti.thebuttonmcchat.PluginMain;
import io.github.norbipeti.thebuttonmcchat.commands.appendtext.TableflipCommand;
import io.github.norbipeti.thebuttonmcchat.commands.appendtext.UnflipCommand;
import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class TBMCCommandBase implements CommandExecutor {

	public static void RegisterCommands(PluginMain plugin) {
		TBMCCommandBase cmd = new UCommand();
		plugin.getCommand(cmd.GetCommandName()).setExecutor(cmd);
		cmd = new OOCCommand();
		plugin.getCommand(cmd.GetCommandName()).setExecutor(cmd);
		cmd = new UnlolCommand();
		plugin.getCommand(cmd.GetCommandName()).setExecutor(cmd);
		cmd = new MWikiCommand();
		plugin.getCommand(cmd.GetCommandName()).setExecutor(cmd);
		cmd = new TableflipCommand();
		plugin.getCommand(cmd.GetCommandName()).setExecutor(cmd);
		cmd = new UnflipCommand();
		plugin.getCommand(cmd.GetCommandName()).setExecutor(cmd);
		cmd = new ChatonlyCommand();
		plugin.getCommand(cmd.GetCommandName()).setExecutor(cmd);
	}

	private static HashMap<String, TBMCCommandBase> commands = new HashMap<String, TBMCCommandBase>();

	public static HashMap<String, TBMCCommandBase> GetCommands() {
		return commands;
	}

	public TBMCCommandBase() {
		commands.put(GetCommandName(), this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		if (GetPlayerOnly() && !(sender instanceof Player)) {
			sender.sendMessage("§cError: You must be a player to use this command.");
			return true;
		}
		if (!OnCommand(sender, alias, args))
			sender.sendMessage(GetHelpText(alias));
		return true;
	}

	public abstract String[] GetHelpText(String alias);

	public abstract boolean OnCommand(CommandSender sender, String alias,
			String[] args);

	public abstract String GetCommandName();

	public abstract boolean GetPlayerOnly();
}
