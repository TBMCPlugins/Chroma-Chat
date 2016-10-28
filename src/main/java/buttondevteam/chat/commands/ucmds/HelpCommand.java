package buttondevteam.chat.commands.ucmds;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;

public final class HelpCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Help ----", "Prints out help messages for the TBMC plugins" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(new String[] { "§6---- TBMC Help ----", "Do /u help <topic> for more info",
					"Do /u help <commandname> [subcommands] for more info about a command", "Topics:",
					"commands: See all the commands from this plugin",
					"chat: Shows some info about custom chat features", "colors: Shows Minecraft color codes" });
			return true;
		}
		if (args[0].equalsIgnoreCase("chat"))
			sender.sendMessage(new String[] { "§6---- Chat features ----",
					"- [g] Channel identifier: Click it to copy message", "-- [g]: Global chat (/g)",
					"-- [TC] Town chat (/tc)", "-- [NC] Nation chat (/nc)",
					"- Playernames: Hover over them to get some player info",
					"-- Respect: This is the number of paid respects divided by eliglble deaths. This is a reference to CoD:AW's \"Press F to pay respects\"" });
		else if (args[0].equalsIgnoreCase("commands")) {
			ArrayList<String> text = new ArrayList<String>();
			text.add("§6---- Command list ----");
			for (TBMCCommandBase cmd : TBMCChatAPI.GetCommands().values())
				if (!cmd.GetCommandPath().contains("/"))
					text.add("/" + cmd.GetCommandPath());
			sender.sendMessage(text.toArray(new String[text.size()]));
		} else if (args[0].equalsIgnoreCase("colors")) {
			sender.sendMessage(new String[] { "§6---- Chat colors/formats ----", //
					"Tellraw name   - Code | Tellraw name    - Code", //
					"§0black        - &0   | §1dark_blue     - &1", //
					"§2dark_green   - &2   | §3dark_aqua     - &3", //
					"§4dark_red     - &4   | §5dark_purple   - &5", //
					"§6gold         - &6   | §7gray          - &7", //
					"§8dark_gray    - &8   | §9blue          - &9", //
					"§agreen        - &a   | §baqua          - &b", //
					"§cred          - &c   | §dlight_purple  - &d", //
					"§eyellow       - &e   | §fwhite         - &f", //
					"§rreset        - &r   | §kk§robfuscated - &k", //
					"§lbold         - &l   | §mstrikethrough - &m", //
					"§nunderline    - &n   | §oitalic        - &o", //
					"The format codes in tellraw should be used like \"italic\":\"true\"" }); //
		} else {
			String path = args[0];
			for (int i = 1; i < args.length; i++)
				path += "/" + args[i];
			TBMCCommandBase cmd = TBMCChatAPI.GetCommands().get(path);
			if (cmd == null)
				sender.sendMessage(new String[] { "§cError: Command not found: " + path.replace('/', ' '),
						"Usage example: /u accept --> /u help u accept" });
			else
				sender.sendMessage(cmd.GetHelpText(args[0]));
		}
		return true;

	}

	@Override
	public String GetUCommandPath() {
		return "help";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}
}
