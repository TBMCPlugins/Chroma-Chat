package io.github.norbipeti.thebuttonmcchat.commands.ucmds;

import io.github.norbipeti.thebuttonmcchat.commands.TBMCCommandBase;

import org.bukkit.command.CommandSender;

public final class HelpCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Help ----",
				"Prints out help messages for the TBMC plugin" };
	}

	@Override
	public boolean OnUCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(new String[] {
					"§6---- TBMC Help ----",
					"Do /u help <subcommand> for more info",
					"Alternatively, you can do /u help <commandname> for more info about a command",
					"Subcommands:",
					"flairs: The flairs are the numbers near your name",
					"commands: See all the commands from this plugin",
					"login: If you or someone else has any problems with logins, lost inventory/location, etc." });
			return true;
		}
		if (args[0].equalsIgnoreCase("flairs"))
			sender.sendMessage(new String[] { "§6---- About flairs ----", "" }); // TODO
		else if (args[0].equalsIgnoreCase("commands")) {
			String[] text = new String[TBMCCommandBase.GetCommands().size() + 1];
			int i = 0;
			text[i++] = "§6---- Command list ----";
			for (TBMCCommandBase cmd : TBMCCommandBase.GetCommands().values())
				text[i++] = "/" + cmd.GetCommandName();
			sender.sendMessage(text);
		} else {
			TBMCCommandBase cmd = TBMCCommandBase.GetCommands().get(args[0]);
			if (cmd == null)
				sender.sendMessage(new String[] {
						"§cError: Command not found: " + args[0],
						"Use either a command of this plugin or a subcommand (for example: /u accept --> /u help accept" });
			else
				sender.sendMessage(cmd.GetHelpText(args[0]));
		}
		return true;
	}

	@Override
	public String GetUCommandName() {
		return "help";
	}
}
