package io.github.norbipeti.thebuttonmcchat.commands;

import io.github.norbipeti.thebuttonmcchat.PluginMain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.reflections.Reflections;

public class CommandCaller implements CommandExecutor {

	private static HashMap<String, TBMCCommandBase> commands = new HashMap<String, TBMCCommandBase>();

	public void RegisterCommands(PluginMain plugin) {
		System.out.println("Registering commands...");
		Reflections rf = new Reflections(
				"io.github.norbipeti.thebuttonmcchat.commands");
		Set<Class<? extends TBMCCommandBase>> cmds = rf
				.getSubTypesOf(TBMCCommandBase.class);
		for (Class<? extends TBMCCommandBase> cmd : cmds) {
			try {
				TBMCCommandBase c = cmd.newInstance();
				commands.put(c.GetCommandPath(), c);
				plugin.getCommand(c.GetCommandPath()).setExecutor(this);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
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
		TBMCCommandBase cmd = commands.get(path);
		int argc = 0;
		while (cmd == null && path.contains("/")) {
			path = path.substring(0, path.indexOf('/'));
			argc++;
			cmd = commands.get(path);
		}
		if (cmd == null) {
			sender.sendMessage("§cInternal error: Command not registered to CommandCaller");
			if (sender != Bukkit.getConsoleSender())
				Bukkit.getConsoleSender()
						.sendMessage(
								"§cInternal error: Command not registered to CommandCaller");
			return true;
		}
		cmd.OnCommand(sender, alias,
				Arrays.copyOfRange(args, argc, args.length - 1));
		return true;
	}
}
