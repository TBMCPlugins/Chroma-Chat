package buttondevteam.chat.commands.ucmds;

import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.CustomTabComplete;
import org.bukkit.command.CommandSender;

@CommandClass(modOnly = false, helpText = {
	"Help",
	"Prints out help messages for the TBMC plugins"
})
public final class HelpCommand extends UCommandBase {
	@Command2.Subcommand
	public boolean def(CommandSender sender, @Command2.TextArg @Command2.OptionalArg
	@CustomTabComplete({"commands", "chat", "colors"}) String topicOrCommand) {
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
				"- [g] Channel identifier: Click it to copy message", "-- [g] Global chat (/g)",
				"-- [TC] Town chat (/tc)", "-- [NC] Nation chat (/nc)",
				"- Playernames: Hover over them to get some player info",
				"-- Respect: This is the number of paid respects divided by eligible deaths. This is a reference to CoD:AW's \"Press F to pay respects\""});
		else if (topicOrCommand.equalsIgnoreCase("commands")) {
			sender.sendMessage(getManager().getCommandsText());
		} else if (topicOrCommand.equalsIgnoreCase("colors")) {
			sender.sendMessage(new String[]{"§6---- Chat colors/formats ----", //
				"Tellraw name - Code | Tellraw name      - Code", //
				"§0black           - &0§r   | §1dark_blue     - &1§r", //
				"§2dark_green   - &2§r   | §3dark_aqua     - &3§r", //
				"§4dark_red     - &4§r   | §5dark_purple   - &5§r", //
				"§6gold             - &6§r   | §7gray          - &7§r", //
				"§8dark_gray     - &8§r   | §9blue          - &9§r", //
				"§agreen           - &a§r   | §baqua          - &b§r", //
				"§cred              - &c§r   | §dlight_purple  - &d§r", //
				"§eyellow           - &e§r   | §fwhite         - &f§r", //
				"§rreset            - &r§r   | §kk§robfuscated - &k§r", //
				"§lbold           - &l§r   | §mstrikethrough - &m§r", //
				"§nunderline      - &n§r   | §oitalic        - &o§r", //
				"The format codes in tellraw should be used like \"italic\":\"true\""}); //
		} else {
			String[] text = getManager().getCommandNode(topicOrCommand).getData().getHelpText(sender); // TODO: This only works for the main command, not subcommands
			if (text == null) // TODO: Null check for command node
				sender.sendMessage(
					new String[]{"§cError: Command not found: " + topicOrCommand,
						"Usage example: /u accept --> /u help u accept"});
			else
				sender.sendMessage(text);
		}
		return true;

	}
}
