package buttondevteam.chat;

import org.bukkit.Bukkit;

import java.util.Optional;

public final class ChatUtils {
	private ChatUtils() {}

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
}
