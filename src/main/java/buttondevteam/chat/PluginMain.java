package buttondevteam.chat;

import buttondevteam.chat.commands.YeehawCommand;
import buttondevteam.chat.components.announce.AnnouncerComponent;
import buttondevteam.chat.components.flair.FlairComponent;
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
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Scoreboard;

public class PluginMain extends ButtonPlugin { // Translated to Java: 2015.07.15.
	// A user, which flair isn't obtainable:
	// https://www.reddit.com/r/thebutton/comments/31c32v/i_pressed_the_button_without_really_thinking/
	public static PluginMain Instance;
	public static ConsoleCommandSender Console;

	public static Scoreboard SB;

	public ConfigData<String> notificationSound() {
		return getIConfig().getData("notificationSound", "");
	}

	public ConfigData<Float> notificationPitch() {
		return getIConfig().getData("notificationPitch", 1.0f);
	}

	// Fired when plugin is first enabled
	@Override
	public void pluginEnable() {
		Instance = this;
		PluginMain.essentials = (Essentials) (Bukkit.getPluginManager().getPlugin("Essentials"));

		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerJoinLeaveListener(), this);
		TBMCChatAPI.AddCommands(this, YeehawCommand.class);
		Console = this.getServer().getConsoleSender();

		SB = getServer().getScoreboardManager().getMainScoreboard(); // Main can be detected with @a[score_...]

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
	}

	public static Essentials essentials = null;

	// Fired when plugin is disabled
	@Override
	public void pluginDisable() {
	}

	public static Permission permission = null;
	public static Economy economy = null;
	public static Chat chat = null;

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
