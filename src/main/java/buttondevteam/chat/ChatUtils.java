package buttondevteam.chat;

import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.ThorpeUtils;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.Function;

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
	 * @param e        The chat event
	 * @param modifier A function that alters the message to be displayed to the player
	 */
	public static void sendChatMessage(TBMCChatEvent e, Function<String, String> modifier) {
		var str = "[" + e.getChannel().DisplayName().get() + "] <"
			+ ThorpeUtils.getDisplayName(e.getSender()) + "> " + e.getMessage();
		str = modifier.apply(str);
		for (Player p : Bukkit.getOnlinePlayers())
			if (e.shouldSendTo(p))
				p.sendMessage(str);
		Bukkit.getConsoleSender().sendMessage(str);
	}
}
