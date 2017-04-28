package buttondevteam.chat.commands.ucmds;

import org.bukkit.command.CommandSender;

import buttondevteam.lib.chat.CommandClass;

@CommandClass(modOnly = false)
public class OpmeCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- OP me ----", "Totally makes you OP" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		sender.sendMessage("It would be nice, wouldn't it?");
		return true;
	}

}
