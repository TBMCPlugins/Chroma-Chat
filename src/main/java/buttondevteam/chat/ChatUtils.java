package buttondevteam.chat;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.ChromaUtils;
import buttondevteam.lib.TBMCChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class ChatUtils {
	public static final String MCORIGIN = "Minecraft"; //Shouldn't change, like ever - TBMCPlayer.getFolderForType(TBMCPlayer.class) capitalized

	private ChatUtils() {
	}

	/**
	 * Dispatch a console command.
	 *
	 * @param command The command
	 * @param async   Whether the caller is async
	 */
	public static void dispatchConsoleCommand(String command, boolean async) {
		if (async)
			Bukkit.getScheduler().runTask(PluginMain.Instance, () -> Bukkit.dispatchCommand(PluginMain.Console, command));
		else
			Bukkit.dispatchCommand(PluginMain.Console, command);
	}

	/**
	 * Returns the string between the start and end strings (exclusive).
	 *
	 * @param str   The original string
	 * @param start The start string
	 * @param end   The end string
	 * @return The result string
	 */
	public static Optional<String> coolSubstring(String str, String start, String end) {
		int a = str.indexOf(start) + start.length();
		int b = str.indexOf(end, a);
		return a != -1 && b != -1 ? Optional.of(str.substring(a, b)) : Optional.empty();
	}

	/**
	 * Sends a regular (non-Markdown) chat message. Used as a fallback if the chat processing fails.
	 *
	 * @param e The chat event
	 */
	public static void sendChatMessage(TBMCChatEvent e) {
		var str = getMessageString(e.getChannel(), e.getSender(), e.getMessage());
		for (Player p : Bukkit.getOnlinePlayers())
			if (e.shouldSendTo(p))
				p.sendMessage(str);
		Bukkit.getConsoleSender().sendMessage(str);
	}

	public static String getMessageString(Channel channel, CommandSender sender, String message) {
		return "§c!§r[" + channel.displayName.get() + "] <"
			+ ChromaUtils.getDisplayName(sender) + "> " + message;
	}

	public static void sendChatMessage(Channel channel, CommandSender sender, String message, CommandSender to) {
		to.sendMessage(getMessageString(channel, sender, message));
	}
}
