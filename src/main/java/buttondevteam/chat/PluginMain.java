package buttondevteam.chat;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import buttondevteam.chat.commands.YeehawCommand;
import buttondevteam.chat.listener.PlayerListener;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.chat.Channel.RecipientTestResult;
import buttondevteam.lib.player.TBMCPlayerBase;

import com.earth2me.essentials.Essentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import java.io.*;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class PluginMain extends JavaPlugin { // Translated to Java: 2015.07.15.
	// A user, which flair isn't obtainable:
	// https://www.reddit.com/r/thebutton/comments/31c32v/i_pressed_the_button_without_really_thinking/
	public static PluginMain Instance;
	public static ConsoleCommandSender Console;
	public final static String FlairThreadURL = "https://www.reddit.com/r/Chromagamers/comments/51ys94/flair_thread_for_the_mc_server/";

	public static Scoreboard SB;
	public static TownyUniverse TU;
	public static ArrayList<Town> Towns;
	public static ArrayList<Nation> Nations;

	public static Channel TownChat;
	public static Channel NationChat;

	/**
	 * <p>
	 * This variable is used as a cache for flair state checking when reading the flair thread.
	 * </p>
	 * <p>
	 * It's used because normally it has to load all associated player files every time to read the flair state
	 * </p>
	 */
	private Set<String> PlayersWithFlairs = new HashSet<>();

	// Fired when plugin is first enabled
	@Override
	public void onEnable() {
		Instance = this;

		TBMCCoreAPI.RegisterEventsForExceptions(new PlayerListener(), this);
		TBMCChatAPI.AddCommands(this, YeehawCommand.class);
		Console = this.getServer().getConsoleSender();
		LoadFiles();

		SB = getServer().getScoreboardManager().getMainScoreboard(); // Main can be detected with @a[score_...]
		TU = ((Towny) Bukkit.getPluginManager().getPlugin("Towny")).getTownyUniverse();
		Towns = new ArrayList<Town>(TU.getTownsMap().values()); // Creates a snapshot of towns, new towns will be added when needed
		Nations = new ArrayList<Nation>(TU.getNationsMap().values()); // Same here but with nations

		TBMCChatAPI.RegisterChatChannel(
				TownChat = new Channel("§3TC§f", Color.DarkAqua, "tc", s -> checkTownNationChat(s, false)));
		TBMCChatAPI.RegisterChatChannel(
				NationChat = new Channel("§6NC§f", Color.Gold, "nc", s -> checkTownNationChat(s, true)));

		setupChat();
		setupEconomy();
		setupPermissions();

		new Thread(() -> FlairGetterThreadMethod()).start();
		new Thread(new AnnouncerThread()).start();
	}

	public Boolean stop = false;
	public static Essentials essentials = null;

	// Fired when plugin is disabled
	@Override
	public void onDisable() {
		SaveFiles();
		stop = true;
	}

	private void FlairGetterThreadMethod() {
		int errorcount = 0;
		while (!stop) {
			try {
				String body = TBMCCoreAPI.DownloadString(FlairThreadURL + ".json?limit=1000");
				JsonArray json = new JsonParser().parse(body).getAsJsonArray().get(1).getAsJsonObject().get("data")
						.getAsJsonObject().get("children").getAsJsonArray();
				for (Object obj : json) {
					JsonObject item = (JsonObject) obj;
					String author = item.get("data").getAsJsonObject().get("author").getAsString();
					String ign = item.get("data").getAsJsonObject().get("body").getAsString();
					int start = ign.indexOf("IGN:") + "IGN:".length();
					if (start == -1 + "IGN:".length())
						continue;
					int end = ign.indexOf(' ', start);
					if (end == -1 || end == start)
						end = ign.indexOf('\n', start);
					if (end == -1 || end == start)
						ign = ign.substring(start);
					else
						ign = ign.substring(start, end);
					ign = ign.trim();
					if (PlayersWithFlairs.contains(ign))
						continue;
					try (ChatPlayer mp = TBMCPlayerBase.getFromName(ign, ChatPlayer.class)) { // Loads player file
						if (mp == null)
							continue;
						/*
						 * if (!JoinedBefore(mp, 2015, 6, 5)) continue;
						 */
						if (!mp.UserNames().contains(author))
							mp.UserNames().add(author);
						if (mp.FlairState().get().equals(FlairStates.NoComment)) {
							mp.FlairState().set(FlairStates.Commented);
							ConfirmUserMessage(mp);
						}
						PlayersWithFlairs.add(ign); // Don't redownload even if flair isn't accepted
					}
				}
			} catch (Exception e) {
				errorcount++;
				if (errorcount >= 10) {
					errorcount = 0;
					if (!e.getMessage().contains("Server returned HTTP response code")
							&& !(e instanceof UnknownHostException))
						TBMCCoreAPI.SendException("Error while getting flairs from Reddit!", e);
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void DownloadFlair(ChatPlayer mp) throws MalformedURLException, IOException {
		String[] flairdata = TBMCCoreAPI
				.DownloadString("http://karmadecay.com/thebutton-data.php?users=" + mp.UserName().get())
				.replace("\"", "").split(":");
		String flair;
		if (flairdata.length > 1)
			flair = flairdata[1];
		else
			flair = "";
		String flairclass;
		if (flairdata.length > 2)
			flairclass = flairdata[2];
		else
			flairclass = "unknown";
		SetFlair(mp, flair, flairclass, mp.UserName().get());
	}

	private void SetFlair(ChatPlayer p, String text, String flairclass, String username) {
		p.UserName().set(username);
		p.FlairState().set(FlairStates.Recognised);
		switch (flairclass) {
		case "cheater":
			p.SetFlair(Short.parseShort(text), true);
			return;
		case "unknown":
			try {
				if (CheckForJoinDate(p)) {
					if (text.equals("-1")) // If true, only non-presser/can't press; if false, any flair (but we can still detect can't press)
						p.SetFlair(ChatPlayer.FlairTimeNonPresser);
					else
						p.SetFlair(ChatPlayer.FlairTimeNone); // Flair unknown
				} else {
					p.SetFlair(ChatPlayer.FlairTimeCantPress);
				}
			} catch (Exception e) {
				p.FlairState().set(FlairStates.Commented); // Flair unknown
				p.SetFlair(ChatPlayer.FlairTimeNone);
				TBMCCoreAPI.SendException("Error while checking join date for player " + p.PlayerName() + "!", e);
			}
			return;
		default:
			break;
		}
		p.SetFlair(Short.parseShort(text));
	}

	public static boolean CheckForJoinDate(ChatPlayer mp) throws Exception {
		return JoinedBefore(mp, 2015, 4, 1);
	}

	public static boolean JoinedBefore(ChatPlayer mp, int year, int month, int day) throws Exception {
		URL url = new URL("https://www.reddit.com/u/" + mp.UserName());
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "TheButtonAutoFlair");
		InputStream in = con.getInputStream();
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode node = cleaner.clean(in);

		node = node.getElementsByAttValue("class", "age", true, true)[0];
		node = node.getElementsByName("time", false)[0];
		String joindate = node.getAttributeByName("datetime");
		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd");
		joindate = joindate.split("T")[0];
		Date date = parserSDF.parse(joindate);
		return date.before(new Calendar.Builder().setTimeZone(TimeZone.getTimeZone("UTC")).setDate(year, month, day)
				.build().getTime());
	}

	public static void ConfirmUserMessage(ChatPlayer mp) {
		Player p = Bukkit.getPlayer(mp.getUUID());
		if (mp.FlairState().get().equals(FlairStates.Commented) && p != null)
			if (mp.UserNames().size() > 1)
				p.sendMessage(
						"§9Multiple Reddit users commented your name. You can select with /u accept.§r §6Type /u accept or /u ignore§r");
			else
				p.sendMessage("§9A Reddit user commented your name. Is that you?§r §6Type /u accept or /u ignore§r");
	}

	public static ArrayList<String> AnnounceMessages = new ArrayList<>();
	public static int AnnounceTime = 15 * 60 * 1000;

	public static void LoadFiles() {
		PluginMain.Instance.getLogger().info("Loading files...");
		try {
			File file = new File("TBMC/chatsettings.yml");
			if (file.exists()) {
				YamlConfiguration yc = new YamlConfiguration();
				yc.load(file);
				PlayerListener.NotificationSound = yc.getString("notificationsound");
				PlayerListener.NotificationPitch = yc.getDouble("notificationpitch");
				AnnounceTime = yc.getInt("announcetime");
				AnnounceMessages.addAll(yc.getStringList("announcements"));
				PlayerListener.AlphaDeaths = yc.getInt("alphadeaths");
			}
			PluginMain.Instance.getLogger().info("Loaded files!");
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
			yc.set("announcetime", AnnounceTime);
			yc.set("announcements", AnnounceMessages);
			yc.set("alphadeaths", PlayerListener.AlphaDeaths);
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

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}

		return (chat != null);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	/**
	 * Return the error message for the message sender if they can't send it and the score
	 */
	private static RecipientTestResult checkTownNationChat(CommandSender sender, boolean nationchat) {
		if (!(sender instanceof Player))
			return new RecipientTestResult("§cYou are not a player!");
		try {
			Resident resident = PluginMain.TU.getResidentMap().get(sender.getName().toLowerCase());
			if (resident != null && resident.getModes().contains("spy"))
				return null;
			/*
			 * p.sendMessage(String.format("[SPY-%s] - %s: %s", channel.DisplayName, ((Player) sender).getDisplayName(), message));
			 */
			Town town = null;
			if (resident != null && resident.hasTown())
				town = resident.getTown();
			if (town == null)
				return new RecipientTestResult("You aren't in a town.");
			Nation nation = null;
			int index = -1;
			if (nationchat) {
				if (town.hasNation())
					nation = town.getNation();
				if (nation == null)
					return new RecipientTestResult("Your town isn't in a nation.");
				index = PluginMain.Nations.indexOf(nation);
				if (index < 0) {
					PluginMain.Nations.add(nation);
					index = PluginMain.Nations.size() - 1;
				}
			} else {
				index = PluginMain.Towns.indexOf(town);
				if (index < 0) {
					PluginMain.Towns.add(town);
					index = PluginMain.Towns.size() - 1;
				}
			}
			return new RecipientTestResult(index);
		} catch (NotRegisteredException e) {
			return new RecipientTestResult("You (probably) aren't knwon by Towny! (Not in a town)");
		}
	}
}
