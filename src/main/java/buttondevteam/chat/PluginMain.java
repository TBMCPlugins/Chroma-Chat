package buttondevteam.chat;

import buttondevteam.chat.commands.MWikiCommand;
import buttondevteam.chat.commands.SnapCommand;
import buttondevteam.chat.commands.ucmds.HelpCommand;
import buttondevteam.chat.commands.ucmds.HistoryCommand;
import buttondevteam.chat.commands.ucmds.InfoCommand;
import buttondevteam.chat.commands.ucmds.ReloadCommand;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.chat.components.announce.AnnouncerComponent;
import buttondevteam.chat.components.appendext.AppendTextComponent;
import buttondevteam.chat.components.flair.FlairComponent;
import buttondevteam.chat.components.formatter.FormatterComponent;
import buttondevteam.chat.components.fun.FunComponent;
import buttondevteam.chat.components.towncolors.TownColorComponent;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.chat.listener.PlayerJoinLeaveListener;
import buttondevteam.chat.listener.PlayerListener;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.ButtonPlugin;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import com.earth2me.essentials.Essentials;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PluginMain extends ButtonPlugin { // Translated to Java: 2015.07.15.
	// A user, which flair isn't obtainable:
	// https://www.reddit.com/r/thebutton/comments/31c32v/i_pressed_the_button_without_really_thinking/
	public static PluginMain Instance;
	public static ConsoleCommandSender Console;

	/**
	 * If enabled, stores and displays the last 10 messages the player can see (public, their town chat etc.)
	 * Can be used with the Discord plugin so players can see some of the conversation they missed that's visible on Discord anyways.
	 */
	public ConfigData<Boolean> storeChatHistory() {
		return getIConfig().getData("storeChatHistory", true);
	}

	// Fired when plugin is first enabled
	@Override
	public void pluginEnable() {
		Instance = this;
		PluginMain.essentials = (Essentials) (Bukkit.getPluginManager().getPlugin("Essentials"));

		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerJoinLeaveListener(), this);
		Console = this.getServer().getConsoleSender();

		if (Bukkit.getPluginManager().isPluginEnabled("Towny"))
			Component.registerComponent(this, new TownyComponent());

		TBMCChatAPI.RegisterChatChannel(new Channel("§7RP§f", Color.Gray, "rp", null)); //Since it's null, it's recognised as global

		if (!setupEconomy() || !setupPermissions())
			TBMCCoreAPI.SendException("We're in trouble", new Exception("Failed to set up economy or permissions!"));

		if (Bukkit.getPluginManager().isPluginEnabled("Towny"))
			Component.registerComponent(this, new TownColorComponent());
		Component.registerComponent(this, new FlairComponent()); //The original purpose of this plugin
		Component.registerComponent(this, new AnnouncerComponent());
		Component.registerComponent(this, new FunComponent());
		Component.registerComponent(this, new AppendTextComponent());
		Component.registerComponent(this, new FormatterComponent());
		getCommand2MC().registerCommand(new DebugCommand());
		getCommand2MC().registerCommand(new HelpCommand());
		getCommand2MC().registerCommand(new HistoryCommand());
		getCommand2MC().registerCommand(new InfoCommand());
		getCommand2MC().registerCommand(new MWikiCommand());
		getCommand2MC().registerCommand(new ReloadCommand());
		getCommand2MC().registerCommand(new SnapCommand());
	}

	public static Essentials essentials = null;

	// Fired when plugin is disabled
	@Override
	public void pluginDisable() {
	}

	public static Permission permission = null;
	public static Economy economy = null;

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager()
			.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
			.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

}
