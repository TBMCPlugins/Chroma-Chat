package buttondevteam.chat;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import buttondevteam.chat.commands.CommandCaller;
import buttondevteam.chat.commands.YeehawCommand;
import buttondevteam.chat.listener.PlayerListener;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.TBMCPlayer;
import buttondevteam.lib.chat.TBMCChatAPI;

import com.earth2me.essentials.Essentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import java.io.*;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class PluginMain extends JavaPlugin { // Translated to Java: 2015.07.15.
	// A user, which flair isn't obtainable:
	// https://www.reddit.com/r/thebutton/comments/31c32v/i_pressed_the_button_without_really_thinking/
	public static PluginMain Instance;
	public static ConsoleCommandSender Console;
	public static Scoreboard SB;
	public final static String FlairThreadURL = "https://www.reddit.com/r/Chromagamers/comments/51ys94/flair_thread_for_the_mc_server/";
	public TownyUniverse TU;
	public ArrayList<Town> Towns;
	public ArrayList<Nation> Nations;
	/**
	 * <p>
	 * This variable is used as a cache for flair state checking when reading the flair thread.
	 * </p>
	 * <p>
	 * It's used because normally it has to load all associated player files every time to read the filename
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

		SB = PluginMain.Instance.getServer().getScoreboardManager().getMainScoreboard(); // Main can be detected with @a[score_...]
		if (SB.getObjective("town") == null)
			SB.registerNewObjective("town", "dummy");
		if (SB.getObjective("nation") == null)
			SB.registerNewObjective("nation", "dummy");
		if (SB.getObjective("admin") == null)
			SB.registerNewObjective("admin", "dummy");
		if (SB.getObjective("mod") == null)
			SB.registerNewObjective("mod", "dummy");
		TU = ((Towny) Bukkit.getPluginManager().getPlugin("Towny")).getTownyUniverse();
		Towns = new ArrayList<Town>(TU.getTownsMap().values());
		Nations = new ArrayList<Nation>(TU.getNationsMap().values());

		setupChat();
		setupEconomy();
		setupPermissions();

		Runnable r = new Runnable() {
			public void run() {
				FlairGetterThreadMethod();
			}
		};
		Thread t = new Thread(r);
		t.start();
		r = new Runnable() {
			public void run() {
				AnnouncerThread.Run();
			}
		};
		t = new Thread(r);
		t.start();
		Bukkit.getScheduler().runTaskLater(this, () -> CommandCaller.RegisterCommands(), 0);
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
					try (ChatPlayer mp = TBMCPlayer.getFromName(ign).asPluginPlayer(ChatPlayer.class)) { // Loads player file
						if (mp == null)
							continue;
						/*
						 * if (!JoinedBefore(mp, 2015, 6, 5)) continue;
						 */
						if (!mp.getUserNames().contains(author))
							mp.getUserNames().add(author);
						if (mp.getFlairState().equals(FlairStates.NoComment)) {
							mp.setFlairState(FlairStates.Commented);
							ConfirmUserMessage(mp);
						}
						PlayersWithFlairs.add(ign); // Don't redownload even if flair isn't accepted
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			} catch (Exception e) {
				LastException = e;
			}
		}
	}

	public void DownloadFlair(ChatPlayer mp) throws MalformedURLException, IOException {
		String[] flairdata = TBMCCoreAPI
				.DownloadString("http://karmadecay.com/thebutton-data.php?users=" + mp.getUserName()).replace("\"", "")
				.split(":");
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
		SetFlair(mp, flair, flairclass, mp.getUserName());
	}

	public static Exception LastException;

	private void SetFlair(ChatPlayer p, String text, String flairclass, String username) {
		p.setUserName(username);
		p.setFlairState(FlairStates.Recognised);
		switch (flairclass) {
		case "cheater":
			p.SetFlair(Short.parseShort(text), true);
			return;
		case "unknown":
			if (text.equals("-1")) // If true, only non-presser/can't press; if
									// false, any flair (but we can still detect
									// can't press)
			{
				try {
					if (CheckForJoinDate(p)) {
						p.SetFlair(ChatPlayer.FlairTimeNonPresser);
					} else {
						p.SetFlair(ChatPlayer.FlairTimeCantPress);
					}
				} catch (Exception e) {
					p.setFlairState(FlairStates.Commented); // Flair unknown
					p.SetFlair(ChatPlayer.FlairTimeNone);
					e.printStackTrace();
				}
			} else {
				try {
					if (CheckForJoinDate(p)) {
						p.setFlairState(FlairStates.Commented); // Flair unknown
						p.SetFlair(ChatPlayer.FlairTimeNone);
					} else {
						p.SetFlair(ChatPlayer.FlairTimeCantPress);
					}
				} catch (Exception e) {
					p.setFlairState(FlairStates.Commented); // Flair unknown
					p.SetFlair(ChatPlayer.FlairTimeNone);
					e.printStackTrace();
				}
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
		URL url = new URL("https://www.reddit.com/u/" + mp.getUserName());
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
		Player p = Bukkit.getPlayer(mp.getUuid());
		if (mp.getFlairState().equals(FlairStates.Commented) && p != null)
			if (mp.getUserNames().size() > 1)
				p.sendMessage(
						"§9Multiple Reddit users commented your name. You can select with /u accept.§r §6Type /u accept or /u ignore§r");
			else
				p.sendMessage("§9A Reddit user commented your name. Is that you?§r §6Type /u accept or /u ignore§r");
	}

	public static Collection<? extends Player> GetPlayers() {
		return Instance.getServer().getOnlinePlayers();
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
		} catch (IOException e) {
			PluginMain.Instance.getLogger().warning("Error!\n" + e);
			LastException = e;
		} catch (InvalidConfigurationException e) {
			PluginMain.Instance.getLogger().warning("Error!\n" + e);
			LastException = e;
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
		} catch (IOException e) {
			PluginMain.Instance.getLogger().warning("Error!\n" + e);
			LastException = e;
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
}
