package buttondevteam.chat;

import buttondevteam.chat.commands.YeehawCommand;
import buttondevteam.chat.commands.ucmds.TownColorCommand;
import buttondevteam.chat.components.TownColorComponent;
import buttondevteam.chat.components.announce.AnnouncerComponent;
import buttondevteam.chat.components.flair.FlairComponent;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.chat.listener.PlayerJoinLeaveListener;
import buttondevteam.chat.listener.PlayerListener;
import buttondevteam.chat.listener.TownyListener;
import buttondevteam.component.channel.Channel;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import com.earth2me.essentials.Essentials;
import lombok.val;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.dynmap.towny.DTBridge;
import org.dynmap.towny.DynmapTownyPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PluginMain extends JavaPlugin { // Translated to Java: 2015.07.15.
	// A user, which flair isn't obtainable:
	// https://www.reddit.com/r/thebutton/comments/31c32v/i_pressed_the_button_without_really_thinking/
	public static PluginMain Instance;
	public static ConsoleCommandSender Console;

	public static Scoreboard SB;

	public static Channel TownChat;
	public static Channel NationChat;

	// Fired when plugin is first enabled
	@Override
	public void onEnable() {
		Instance = this;
		PluginMain.essentials = (Essentials) (Bukkit.getPluginManager().getPlugin("Essentials"));

		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerJoinLeaveListener(), this);
		TBMCCoreAPI.RegisterEventsForExceptions(new TownyListener(), this);
		TBMCChatAPI.AddCommands(this, YeehawCommand.class);
		Console = this.getServer().getConsoleSender();
		LoadFiles();

		SB = getServer().getScoreboardManager().getMainScoreboard(); // Main can be detected with @a[score_...]

		Component.registerComponent(this, new TownyComponent());
		TownColors.keySet().removeIf(t -> !TownyComponent.TU.getTownsMap().containsKey(t)); // Removes town colors for deleted/renamed towns
		NationColor.keySet().removeIf(n -> !TownyComponent.TU.getNationsMap().containsKey(n)); // Removes nation colors for deleted/renamed nations

		TBMCChatAPI.RegisterChatChannel(new Channel("§7RP§f", Color.Gray, "rp", null)); //Since it's null, it's recognised as global

		Bukkit.getScheduler().runTask(this, () -> {
			val dtp = (DynmapTownyPlugin) Bukkit.getPluginManager().getPlugin("Dynmap-Towny");
			if (dtp == null)
				return;
			for (val entry : TownColors.entrySet())
                setTownColor(dtp, buttondevteam.chat.commands.ucmds.admin.TownColorCommand.getTownNameCased(entry.getKey()), entry.getValue());
		});

		if (!setupEconomy() || !setupPermissions())
			TBMCCoreAPI.SendException("We're in trouble", new Exception("Failed to set up economy or permissions!"));

		Component.registerComponent(this, new TownColorComponent());
		Component.registerComponent(this, new FlairComponent()); //The original purpose of this plugin
		Component.registerComponent(this, new AnnouncerComponent());
	}

    /**
     * Sets a town's color on Dynmap.
     *
     * @param dtp    A reference for the Dynmap-Towny plugin
     * @param town   The town's name using the correct casing
     * @param colors The town's colors
     */
    public static void setTownColor(DynmapTownyPlugin dtp, String town, Color[] colors) {
        Function<Color, Integer> c2i = c -> c.getRed() << 16 | c.getGreen() << 8 | c.getBlue();
        try {
            DTBridge.setTownColor(dtp, town, c2i.apply(colors[0]),
                    c2i.apply(colors.length > 1 ? colors[1] : colors[0]));
        } catch (Exception e) {
            TBMCCoreAPI.SendException("Failed to set town color for town " + town + "!", e);
        }
    }

	public static Essentials essentials = null;

	// Fired when plugin is disabled
	@Override
	public void onDisable() {
		SaveFiles();
	}

	/**
	 * Names lowercased
	 */
	public static Map<String, Color[]> TownColors = new HashMap<>();
	/**
	 * Names lowercased - nation color gets added to town colors when needed
	 */
	public static Map<String, Color> NationColor = new HashMap<>();

	@SuppressWarnings("unchecked")
	private static void LoadFiles() {
		PluginMain.Instance.getLogger().info("Loading files...");
		try {
			File file = new File("TBMC/chatsettings.yml");
			if (file.exists()) {
				YamlConfiguration yc = new YamlConfiguration();
				yc.load(file);
				PlayerListener.NotificationSound = yc.getString("notificationsound");
				PlayerListener.NotificationPitch = yc.getDouble("notificationpitch");
				val cs = yc.getConfigurationSection("towncolors");
				if (cs != null)
					TownColors.putAll(cs.getValues(true).entrySet().stream()
							.collect(Collectors.toMap(Map.Entry::getKey, v -> ((List<String>) v.getValue()).stream()
									.map(Color::valueOf).toArray(Color[]::new))));
				TownColorCommand.ColorCount = (byte) yc.getInt("towncolorcount", 1);
				val ncs = yc.getConfigurationSection("nationcolors");
				if (ncs != null)
					NationColor.putAll(ncs.getValues(true).entrySet().stream()
							.collect(Collectors.toMap(Map.Entry::getKey, v -> Color.valueOf((String) v.getValue()))));
				PluginMain.Instance.getLogger().info("Loaded files!");
			} else
				PluginMain.Instance.getLogger().info("No files to load, first run probably.");
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while loading chat files!", e);
		}
	}

	public static void SaveFiles() {
		PluginMain.Instance.getLogger().info("Saving files...");
		try {
			File file = new File("TBMC/chatsettings.yml");
			YamlConfiguration yc = new YamlConfiguration();
			yc.set("notificationsound", PlayerListener.NotificationSound);
			yc.set("notificationpitch", PlayerListener.NotificationPitch);
			yc.createSection("towncolors", TownColors.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
					v -> Arrays.stream(v.getValue()).map(Enum::toString).toArray(String[]::new))));
			yc.set("towncolorcount", TownColorCommand.ColorCount);
			yc.createSection("nationcolors", NationColor.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
					v -> v.getValue().toString())));
			yc.save(file);
			PluginMain.Instance.getLogger().info("Saved files!");
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while loading chat files!", e);
		}
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
