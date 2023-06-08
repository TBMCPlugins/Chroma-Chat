package buttondevteam.chat.commands;

import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.chat.*;

@CommandClass(helpText = {
	"Me",
	"Displays a message starting with your name in your current channel."
})
public class MeCommand extends ICommand2MC {
	@Command2.Subcommand
	public void def(Command2MCSender sender, @Command2.TextArg String message) {
		TBMCChatAPI.SendSystemMessage(sender.getChannel(), sender.getChannel().getRTR(sender.getPermCheck()), "§5* " + sender.getSender().getName() + " " + message, TBMCSystemChatEvent.BroadcastTarget.ALL);
	}
}
