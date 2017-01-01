package buttondevteam.chat.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.lib.TBMCPlayer;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.TBMCCommandBase;

public class WaitWhatCommand extends TBMCCommandBase {
	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		Channel channel;
		if (sender instanceof Player && ((Player) sender).isOnline())
			channel = TBMCPlayer.getPlayer((Player) sender).asPluginPlayer(ChatPlayer.class).CurrentChannel;
		else
			channel = Channel.GlobalChat;
		final String message;
		if (args.length == 0)
			message = "wait what";
		else
			message = "wait " + Arrays.stream(args).collect(Collectors.joining(" ")) + " what";
		TBMCChatAPI.SendChatMessage(channel, sender, message);
		return true;
	}

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { //
				"§6--- Wait what ----", //
				"Wait what" //
		};
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

	@Override
	public boolean GetModOnly() {
		return false;
	}
}
