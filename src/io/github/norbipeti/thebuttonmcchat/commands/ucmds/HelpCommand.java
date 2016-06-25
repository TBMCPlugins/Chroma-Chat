package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import java.util.ArrayList;

import io.github.norbipeti.thebuttonmcchat.commands.CommandCaller;
import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;

import org.bukkit.command.CommandSender;

public final class HelpCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Help ----",
				"Prints out help messages for the TBMC plugin" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(new String[] {
					"§6---- TBMC Help ----",
					"Do /u help <topic> for more info",
					"Alternatively, you can do /u help <commandname> [subcommands] for more info about a command",
					"Topics:",
					"flairs: The flairs are the numbers near your name",
					"commands: See all the commands from this plugin",
					"login: If you or someone else has any problems with logins, lost inventory/location, etc." });
			return true;
		}
		if (args[0].equalsIgnoreCase("flairs"))
			sender.sendMessage(new String[] { "§6---- About flairs ----", "" }); // TODO
		else if (args[0].equalsIgnoreCase("commands")) {
			ArrayList<String> text = new ArrayList<String>();
			int i = 0;
			text.set(i++, "§6---- Command list ----");
			for (TBMCCommandBase cmd : CommandCaller.GetCommands().values())
				if (!cmd.GetCommandPath().contains("/"))
					text.set(i++, "/" + cmd.GetCommandPath());
			sender.sendMessage((String[]) text.toArray());
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
}
