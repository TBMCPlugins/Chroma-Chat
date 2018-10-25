package buttondevteam.chat.commands.appendtext;

import buttondevteam.lib.chat.ChatMessage;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;
import buttondevteam.lib.player.ChromaGamerBase;
import org.bukkit.command.CommandSender;

@CommandClass(modOnly = false, excludeFromPath = true)
public abstract class AppendTextCommandBase extends TBMCCommandBase {

	public abstract String[] GetHelpText(String alias);

	public abstract String GetAppendedText();

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		StringBuilder msg = new StringBuilder();
		for (String arg : args) msg.append(arg).append(" ");
		msg.append(GetAppendedText());
		TBMCChatAPI.SendChatMessage(ChatMessage.builder(sender,
				ChromaGamerBase.getFromSender(sender), msg.toString())
				.fromCommand(true).build());
		return true;
	}
}
