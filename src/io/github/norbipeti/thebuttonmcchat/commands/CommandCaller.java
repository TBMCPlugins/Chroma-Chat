package io.github.norbipeti.thebuttonmcchat.commands;

import io.github.norbipeti.thebuttonmcchat.PluginMain;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class CommandCaller implements CommandExecutor {

	private CommandCaller() {
	}

	private static HashMap<String, TBMCCommandBase> commands = new HashMap<String, TBMCCommandBase>();

	public static HashMap<String, TBMCCommandBase> GetCommands() {
		return commands;
	}

	private static CommandCaller instance;

	public static void RegisterCommands(PluginMain plugin) {
		if (instance != null) {
			new Exception("Only one instance of CommandCaller is allowed")
					.printStackTrace();
			return;
		}
		System.out.println("Registering commands...");

		CommandCaller cc = new CommandCaller();
		instance = cc;
		Reflections rf = new Reflections(
				new ConfigurationBuilder()
						.setUrls(
								ClasspathHelper.forClassLoader(plugin
										.getClass().getClassLoader()))
						.addClassLoader(plugin.getClass().getClassLoader())
						.addScanners(new SubTypesScanner())
						.filterInputsBy(
								(String pkg) -> pkg
										.contains("io.github.norbipeti.thebuttonmcchat.commands")));
		Set<Class<? extends TBMCCommandBase>> cmds = rf
				.getSubTypesOf(TBMCCommandBase.class);
		for (Class<? extends TBMCCommandBase> cmd : cmds) {
			try {
				if (Modifier.isAbstract(cmd.getModifiers()))
					continue;
				TBMCCommandBase c = cmd.newInstance();
				commands.put(c.GetCommandPath(), c);
				if (!c.GetCommandPath().contains("/")) // Top-level command
				{
					PluginCommand pc = plugin.getCommand(c.GetCommandPath());
					if (pc == null)
						System.out.println("Can't find top-level command: "
								+ c.GetCommandPath());
					else
						pc.setExecutor(cc);
				}
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
			path = path.substring(0, path.lastIndexOf('/'));
			argc++;
			cmd = commands.get(path);
			System.out.println(path);
		}
		if (cmd == null) {
			sender.sendMessage("§cInternal error: Command not registered to CommandCaller");
			if (sender != Bukkit.getConsoleSender())
				Bukkit.getConsoleSender()
						.sendMessage(
								"§cInternal error: Command not registered to CommandCaller");
			return true;
		}
		if (cmd.GetPlayerOnly() && sender == Bukkit.getConsoleSender()) {
			sender.sendMessage("§cOnly ingame players can use this command.");
			return true;
		}
		if (!cmd.OnCommand(
				sender,
				alias,
				(args.length > 0 ? Arrays.copyOfRange(args, args.length - argc,
						args.length) : args)))
			sender.sendMessage(cmd.GetHelpText(alias));
		return true;
	}

	public static String[] GetSubCommands(TBMCCommandBase command) {
		ArrayList<String> cmds = new ArrayList<String>();
		cmds.add("§6---- Subcommands ----");
		for (TBMCCommandBase cmd : CommandCaller.GetCommands().values()) {
			if (cmd.GetCommandPath().startsWith(command.GetCommandPath() + "/")) {
				int ind = cmd.GetCommandPath().indexOf('/',
						command.GetCommandPath().length() + 2);
				if (ind >= 0)
					continue;
				cmds.add(cmd.GetCommandPath().replace('/', ' '));
			}
		}
		return cmds.toArray(new String[cmds.size()]);
	}
}
