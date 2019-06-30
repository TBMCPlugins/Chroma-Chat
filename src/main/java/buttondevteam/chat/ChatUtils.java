package buttondevteam.chat;

import org.bukkit.Bukkit;

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
}
