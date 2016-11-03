package buttondevteam.chat.commands;

import java.util.Arrays;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;

public class CommandCaller implements CommandExecutor {

	private CommandCaller() {
	}

	private static CommandCaller instance;

	public static void RegisterCommands() {
		if (instance == null)
			instance = new CommandCaller();
		for (Entry<String, TBMCCommandBase> entry : TBMCChatAPI.GetCommands().entrySet()) {
			TBMCCommandBase c = entry.getValue();
			if (c == null) {
				TBMCCoreAPI.SendException("An error occured while registering commands",
						new Exception("Null command found at " + entry.getKey() + "!"));
				continue;
			}
			if (!c.GetCommandPath().contains(" ")) // Top-level command
			{
				PluginCommand pc = ((JavaPlugin) c.getPlugin()).getCommand(c.GetCommandPath());
				if (pc == null)
					TBMCCoreAPI.SendException("An error occured while registering commands",
							new Exception("Can't find top-level command: " + c.GetCommandPath() + " for plugin: "
									+ c.getPlugin().getName()));
				else
					pc.setExecutor(instance);
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String path = command.getName();
		for (String arg : args)
			path += "/" + arg;
		TBMCCommandBase cmd = TBMCChatAPI.GetCommands().get(path);
		int argc = 0;
		while (cmd == null && path.contains("/")) {
			path = path.substring(0, path.lastIndexOf('/'));
			argc++;
			cmd = TBMCChatAPI.GetCommands().get(path);
		}
		if (cmd == null) {
			sender.sendMessage("§cInternal error: Command not registered to CommandCaller");
			if (sender != Bukkit.getConsoleSender())
				Bukkit.getConsoleSender().sendMessage("§cInternal error: Command not registered to CommandCaller");
			return true;
		}
		if (cmd.GetModOnly() && !PluginMain.permission.has(sender, "tbmc.admin")) {
			sender.sendMessage("§cYou need to be a mod to use this command.");
			return true;
		}
		if (cmd.GetPlayerOnly() && !(sender instanceof Player)) {
			sender.sendMessage("§cOnly ingame players can use this command.");
			return true;
		}
		final String[] cmdargs = args.length > 0 ? Arrays.copyOfRange(args, args.length - argc, args.length) : args;
		try {
			if (!cmd.OnCommand(sender, alias, cmdargs))
				sender.sendMessage(cmd.GetHelpText(alias));
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Failed to execute command /" + cmd.GetCommandPath() + " with arguments "
					+ Arrays.toString(cmdargs), e);
		}
		return true;
	}
}
