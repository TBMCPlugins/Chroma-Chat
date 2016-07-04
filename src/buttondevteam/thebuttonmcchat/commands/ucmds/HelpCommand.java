package buttondevteam.thebuttonmcchat.commands.ucmds;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

import buttondevteam.thebuttonmcchat.commands.CommandCaller;
import buttondevteam.thebuttonmcchat.commands.TBMCCommandBase;

public final class HelpCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Help ----",
				"Prints out help messages for the TBMC plugins" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(new String[] {
					"§6---- TBMC Help ----",
					"Do /u help <topic> for more info",
					"Do /u help <commandname> [subcommands] for more info about a command",
					"Topics:",
					"newp: Info for new players",
					"commands: See all the commands from this plugin",
					"login: If you or someone else has any problems with logins, lost inventory/location, etc." });
			return true;
		}
		if (args[0].equalsIgnoreCase("newp"))
			sender.sendMessage(new String[] { "§6---- Info for new players ----", "(Under construction)" }); // TODO
		else if (args[0].equalsIgnoreCase("commands")) {
			ArrayList<String> text = new ArrayList<String>();
			text.add("§6---- Command list ----");
			for (TBMCCommandBase cmd : CommandCaller.GetCommands().values())
				if (!cmd.GetCommandPath().contains("/"))
					text.add("/" + cmd.GetCommandPath());
			sender.sendMessage(text.toArray(new String[text.size()]));
		} else {
			String path = args[0];
			for (int i = 1; i < args.length; i++)
				path += "/" + args[i];
			TBMCCommandBase cmd = CommandCaller.GetCommands().get(path);
			if (cmd == null)
				sender.sendMessage(new String[] {
						"§cError: Command not found: " + path.replace('/', ' '),
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
