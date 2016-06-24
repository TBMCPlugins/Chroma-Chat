package io.github.norbipeti.thebuttonmcchat.commands;

import io.github.norbipeti.thebuttonmcchat.PluginMain;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.reflections.Reflections;

public class CommandCaller implements CommandExecutor {
	private CommandCaller() {
	}

	private static HashMap<String, TBMCCommandBase> commands = new HashMap<String, TBMCCommandBase>();
	private static HashMap<String, TBMCSubCommandBase> subcommands = new HashMap<String, TBMCSubCommandBase>();

	public static void RegisterCommands(PluginMain plugin) {
		System.out.println("Registering commands...");
		Reflections rf = new Reflections(
				"io.github.norbipeti.thebuttonmcchat.commands");
		Set<Class<? extends TBMCCommandBase>> cmds = rf
				.getSubTypesOf(TBMCCommandBase.class);
		for (Class<? extends TBMCCommandBase> cmd : cmds) {
			try {
				TBMCCommandBase c = cmd.newInstance();
				commands.put(c.GetCommandName(), c);
				plugin.getCommand(c.GetCommandName()).setExecutor(c);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Registering subcommands...");
		Set<Class<? extends TBMCSubCommandBase>> subcmds = rf
				.getSubTypesOf(TBMCSubCommandBase.class);
		for (Class<? extends TBMCSubCommandBase> subcmd : subcmds) {
			try {
				TBMCSubCommandBase sc = subcmd.newInstance();
				subcommands.put(sc.GetCommandPath(), sc);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done registering");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String alias, String[] args) {
		// TODO: Test if path exists, if not,
		// go up one level, and
		// finally fallback to args.length==0
		String path = command.getName();
		for (String arg : args)
			path += "/" + arg;
		TBMCSubCommandBase cmd = subcommands.get(path);
		while (cmd == null && path.contains("/"))
			path = path.substring(0, path.indexOf('/'));
		if (cmd == null) {
			sender.sendMessage("§cInternal error: Subcommand not registered to CommandCaller");
			if (sender != Bukkit.getConsoleSender())
				Bukkit.getConsoleSender()
						.sendMessage(
								"§cInternal error: Subcommand not registered to CommandCaller");
			return true;
		}
		return true;
	}
}
