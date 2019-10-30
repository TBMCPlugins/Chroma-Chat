package buttondevteam.chat.commands.ucmds;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandClass(modOnly = false, helpText = {
	"Help",
	"Prints out help messages for the TBMC plugins"
})
public final class HelpCommand extends UCommandBase {
	@Command2.Subcommand
	public boolean def(CommandSender sender, @Command2.TextArg @Command2.OptionalArg String topicOrCommand) {
		if (topicOrCommand == null) {
			sender.sendMessage(new String[]{
				"§6---- Chroma Help ----",
				"Do /u help <topic> for more info",
				"Do /u help <commandname> [subcommands] for more info about a command",
				"Topics:",
				"commands: See all the commands from this plugin",
				"chat: Shows some info about custom chat features",
				"colors: Shows Minecraft color codes"
			});
			return true;
		}
		if (topicOrCommand.equalsIgnoreCase("chat"))
			sender.sendMessage(new String[]{"§6---- Chat features ----",
				"- [g] Channel identifier: Click it to copy message", "-- [g]: Global chat (/g)",
				"-- [TC] Town chat (/tc)", "-- [NC] Nation chat (/nc)",
				"- Playernames: Hover over them to get some player info",
				"-- Respect: This is the number of paid respects divided by eliglble deaths. This is a reference to CoD:AW's \"Press F to pay respects\""});
		else if (topicOrCommand.equalsIgnoreCase("commands")) {
			ArrayList<String> text = new ArrayList<String>();
			text.add("§6---- Command list ----");
			for (TBMCCommandBase cmd : TBMCChatAPI.GetCommands().values())
				if (!cmd.getClass().getAnnotation(CommandClass.class).modOnly() || PluginMain.permission.has(sender, "tbmc.admin"))
					if (!cmd.isPlayerOnly() || sender instanceof Player)
						if (!cmd.GetCommandPath().contains(" "))
							text.add("/" + cmd.GetCommandPath());
						else {
							final String topcmd = cmd.GetCommandPath().substring(0, cmd.GetCommandPath().indexOf(' '));
							if (!text.contains("/" + topcmd))
								text.add("/" + topcmd);
						}
			sender.sendMessage(text.toArray(new String[0]));
		} else if (topicOrCommand.equalsIgnoreCase("colors")) {
			sender.sendMessage(new String[]{"§6---- Chat colors/formats ----", //
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
				"The format codes in tellraw should be used like \"italic\":\"true\""}); //
		} else {
			String[] text = getManager().getHelpText(topicOrCommand);
			if (text == null)
					sender.sendMessage(
						new String[]{"§cError: Command not found: " + topicOrCommand,
							"Usage example: /u accept --> /u help u accept"});
			else
				sender.sendMessage(text);
		}
		return true;

	}
}
