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

	private static final String REGISTER_ERROR_MSG = "An error occured while registering commands";

	private CommandCaller() {
	}

	private static CommandCaller instance;

	public static void RegisterCommands() {
		if (instance == null)
			instance = new CommandCaller();
		for (Entry<String, TBMCCommandBase> entry : TBMCChatAPI.GetCommands().entrySet()) {
			TBMCCommandBase c = entry.getValue();
			if (c == null) {
				TBMCCoreAPI.SendException(REGISTER_ERROR_MSG,
						new Exception("Null command found at " + entry.getKey() + "!"));
				continue;
			}
			if (c.GetCommandPath() == null) {
				TBMCCoreAPI.SendException(REGISTER_ERROR_MSG,
						new Exception("Command " + entry.getKey() + " has no command path!"));
				continue;
			}
			if (c.getPlugin() == null) {
				TBMCCoreAPI.SendException(REGISTER_ERROR_MSG,
						new Exception("Command " + entry.getKey() + " has no plugin!"));
				continue;
			}
			int i;
			String topcmd;
			if ((i = (topcmd = c.GetCommandPath()).indexOf(' ')) != -1) // Get top-level command
				topcmd = c.GetCommandPath().substring(0, i);
			{
				PluginCommand pc = ((JavaPlugin) c.getPlugin()).getCommand(topcmd);
				if (pc == null)
					TBMCCoreAPI.SendException(REGISTER_ERROR_MSG, new Exception("Top level command " + topcmd
							+ " not registered in plugin.yml for plugin: " + c.getPlugin().getName()));
				else
					pc.setExecutor(instance);
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String path = command.getName();
		for (String arg : args)
			path += " " + arg;
		TBMCCommandBase cmd = TBMCChatAPI.GetCommands().get(path);
		int argc = 0;
		boolean hadspace = false;
		while (cmd == null && path.contains(" ")) {
			hadspace = true;
			path = path.substring(0, path.lastIndexOf(' '));
			argc++;
			cmd = TBMCChatAPI.GetCommands().get(path);
		}
		if (cmd == null) {
			if (hadspace) {
				sender.sendMessage(TBMCChatAPI.GetSubCommands(path));
				return true;
			}
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
			if (!cmd.OnCommand(sender, alias, cmdargs)) {
				if (cmd.GetHelpText(alias) == null) {
					sender.sendMessage("Wrong usage, but there's no help text! Error is being reported to devs.");
					throw new NullPointerException("GetHelpText is null for comand /" + cmd.GetCommandPath());
				} else
					sender.sendMessage(cmd.GetHelpText(alias));
			}
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Failed to execute command /" + cmd.GetCommandPath() + " with arguments "
					+ Arrays.toString(cmdargs), e);
		}
		return true;
	}
}
