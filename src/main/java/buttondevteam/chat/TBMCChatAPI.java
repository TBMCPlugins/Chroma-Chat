package buttondevteam.chat;

import org.bukkit.plugin.java.JavaPlugin;

import buttondevteam.chat.commands.CommandCaller;
import buttondevteam.chat.commands.TBMCCommandBase;
import buttondevteam.chat.commands.ucmds.admin.PlayerInfoCommand;

public class TBMCChatAPI {

	/**
	 * <p>
	 * This method adds a plugin's commands to help and sets their executor.
	 * </p>
	 * <p>
	 * The <u>command must be registered</u> in the caller plugin's plugin.yml.
	 * Otherwise the plugin will output a messsage to console.
	 * </p>
	 * 
	 * @param plugin
	 *            The caller plugin
	 * @param acmdclass
	 *            A command's class to get the package name for commands. The
	 *            provided class's package and subpackages are scanned for
	 *            commands.
	 */
	public void AddCommands(JavaPlugin plugin, Class<? extends TBMCCommandBase> acmdclass) {
		CommandCaller.AddCommands(plugin, acmdclass); // TODO: Make it scan for all "buttondevteam" packages
	}

	/**
	 * <p>
	 * Add player information for {@link PlayerInfoCommand}. Only mods can see
	 * the given information.
	 * </p>
	 * 
	 * @param player
	 * @param infoline
	 */
	public void AddPlayerInfoForMods(ChatPlayer player, String infoline) {
		// TODO
	}

	/**
	 * <p>
	 * Add player information for hover text at {@link ChatProcessing}. Every
	 * online player can see the given information.
	 * </p>
	 * 
	 * @param player
	 * @param infoline
	 */
	public void AddPlayerInfoForHover(ChatPlayer player, String infoline) {
		// TODO
	}
}
