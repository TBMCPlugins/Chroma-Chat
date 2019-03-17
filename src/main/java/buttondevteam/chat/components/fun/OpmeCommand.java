package buttondevteam.chat.components.fun;

import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.command.CommandSender;

@CommandClass(modOnly = false, path = "u opme", helpText = {
	"OP me",
	"Totally makes you OP"
})
public class OpmeCommand extends ICommand2MC {
	@Command2.Subcommand
	public boolean def(CommandSender sender) {
		sender.sendMessage("It would be nice, wouldn't it?");
		return true;
	}
}
