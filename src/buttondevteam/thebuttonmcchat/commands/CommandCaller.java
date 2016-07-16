package buttondevteam.thebuttonmcchat.commands;

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
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import buttondevteam.thebuttonmcchat.PluginMain;

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
			new Exception("Only one instance of CommandCaller is allowed").printStackTrace();
			return;
		}
		System.out.println("Registering commands...");

		if (instance == null)
			instance = new CommandCaller();
		Reflections rf = new Reflections(
				new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader(plugin.getClass().getClassLoader()))
						.addClassLoader(plugin.getClass().getClassLoader()).addScanners(new SubTypesScanner())
						.filterInputsBy((String pkg) -> pkg.contains("io.github.norbipeti.thebuttonmcchat.commands")));
		Set<Class<? extends TBMCCommandBase>> cmds = rf.getSubTypesOf(TBMCCommandBase.class);
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
						new Exception("Can't find top-level command: " + c.GetCommandPath()).printStackTrace();
					else
						pc.setExecutor(instance);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static void AddCommand(JavaPlugin plugin, TBMCCommandBase cmd) {
		if (instance == null)
			instance = new CommandCaller();
		commands.put(cmd.GetCommandPath(), cmd);
		if (!cmd.GetCommandPath().contains("/")) // Top-level command
		{
			PluginCommand pc = plugin.getCommand(cmd.GetCommandPath());
			if (pc == null)
				new Exception("Can't find top-level command: " + cmd.GetCommandPath()).printStackTrace();
			else
				pc.setExecutor(instance);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String path = command.getName();
		for (String arg : args)
			path += "/" + arg;
		TBMCCommandBase cmd = commands.get(path);
		int argc = 0;
		while (cmd == null && path.contains("/")) {
			path = path.substring(0, path.lastIndexOf('/'));
			argc++;
			cmd = commands.get(path);
		}
		if (cmd == null) {
			sender.sendMessage("§cInternal error: Command not registered to CommandCaller");
			if (sender != Bukkit.getConsoleSender())
				Bukkit.getConsoleSender().sendMessage("§cInternal error: Command not registered to CommandCaller");
			return true;
		}
		if (cmd.GetModOnly() && PluginMain.permission.has(sender, "tbmc.admin")) {
			sender.sendMessage("§cYou need to be a mod to use this command.");
			return true;
		}
		if (cmd.GetPlayerOnly() && !(sender instanceof Player)) {
			sender.sendMessage("§cOnly ingame players can use this command.");
			return true;
		}
		if (!cmd.OnCommand(sender, alias,
				(args.length > 0 ? Arrays.copyOfRange(args, args.length - argc, args.length) : args)))
			sender.sendMessage(cmd.GetHelpText(alias));
		return true;
	}

	public static String[] GetSubCommands(TBMCCommandBase command) {
		ArrayList<String> cmds = new ArrayList<String>();
		cmds.add("§6---- Subcommands ----");
		for (TBMCCommandBase cmd : CommandCaller.GetCommands().values()) {
			if (cmd.GetCommandPath().startsWith(command.GetCommandPath() + "/")) {
				int ind = cmd.GetCommandPath().indexOf('/', command.GetCommandPath().length() + 2);
				if (ind >= 0)
					continue;
				cmds.add(cmd.GetCommandPath().replace('/', ' '));
			}
		}
		return cmds.toArray(new String[cmds.size()]);
	}
}
