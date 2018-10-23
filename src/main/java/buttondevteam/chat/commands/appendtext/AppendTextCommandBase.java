package buttondevteam.chat.commands.appendtext;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.listener.PlayerListener;
import buttondevteam.lib.chat.*;
import buttondevteam.lib.player.TBMCPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandClass(modOnly = false, excludeFromPath = true)
public abstract class AppendTextCommandBase extends TBMCCommandBase {

	public abstract String[] GetHelpText(String alias);

	public abstract String GetAppendedText();

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		String msg = GetAppendedText();
		for (int i = args.length - 1; i >= 0; i--)
			msg = args[i] + " " + msg;
        ChatPlayer cp;
		if (sender instanceof Player)
            TBMCChatAPI.SendChatMessage(ChatMessage.builder(
                    (cp = TBMCPlayer.getPlayer(((Player) sender).getUniqueId(), ChatPlayer.class)).CurrentChannel, sender,
                    cp, msg).fromCommand(true).build());
		else if (sender.isOp())
            TBMCChatAPI.SendChatMessage(ChatMessage.builder(PlayerListener.ConsoleChannel, sender,
		            (cp = TBMCPlayer.getPlayer(new UUID(0, 0), ChatPlayer.class)), msg).fromCommand(true).build());
		else
            TBMCChatAPI.SendChatMessage(ChatMessage.builder(Channel.GlobalChat, sender,
		            (cp = TBMCPlayer.getPlayer(new UUID(0, 0), ChatPlayer.class)), msg).fromCommand(true).build()); //TODO
		return true;
	}
}
