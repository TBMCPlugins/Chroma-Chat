package buttondevteam.thebuttonmcchat;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.apache.commons.io.IOUtils;
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
import org.json.JSONArray;
import org.json.JSONObject;

import au.com.mineauz.minigames.mechanics.GameMechanics;
import buttondevteam.core.TBMCCoreAPI;
import buttondevteam.thebuttonmcchat.commands.CommandCaller;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import java.io.*;
import java.lang.String;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

public class PluginMain extends JavaPlugin { // Translated to Java: 2015.07.15.
	// A user, which flair isn't obtainable:
	// https://www.reddit.com/r/thebutton/comments/31c32v/i_pressed_the_button_without_really_thinking/
	public static PluginMain Instance;
	public static ConsoleCommandSender Console; // 2015.08.12.
	public static Scoreboard SB;
	public final static String FlairThreadURL = "https://www.reddit.com/r/TheButtonMinecraft/comments/433ptk/autoflair_thread/";
	public TownyUniverse TU;
	public ArrayList<Town> Towns;
	public ArrayList<Nation> Nations;

	// Fired when plugin is first enabled
	@Override
	public void onEnable() {
		try {
			PluginMain.Instance.getLogger().info("Extracting necessary libraries...");
			final File[] libs = new File[] { new File(getDataFolder(), "htmlcleaner-2.16.jar"),
					new File(getDataFolder(), "reflections-0.9.10.jar"),
					new File(getDataFolder(), "javassist-3.19.0-GA.jar") };
			for (final File lib : libs) {
				if (!lib.exists()) {
					JarUtils.extractFromJar(lib.getName(), lib.getAbsolutePath());
				}
			}
			for (final File lib : libs) {
				if (!lib.exists()) {
					getLogger().warning("Failed to load plugin! Could not find lib: " + lib.getName());
					Bukkit.getServer().getPluginManager().disablePlugin(this);
					return;
				}
				addClassPath(JarUtils.getJarUrl(lib));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		CommandCaller.RegisterChatCommands(this);
		Instance = this;
		Console = this.getServer().getConsoleSender();
		LoadFiles(false);

		SB = PluginMain.Instance.getServer().getScoreboardManager().getMainScoreboard(); // Main
																							// can
																							// be
																							// detected
																							// with
																							// @a[score_...]
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

		GameMechanics.addGameMechanic(new CreativeGlobalMechanic());

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
	}

	public Boolean stop = false;

	// Fired when plugin is disabled
	@Override
	public void onDisable() {
		SaveFiles(); // 2015.08.09.
		stop = true;
	}

	private void FlairGetterThreadMethod() {
		while (!stop) {
			try {
				String body = TBMCCoreAPI.DownloadString(FlairThreadURL + ".json?limit=1000");
				JSONArray json = new JSONArray(body).getJSONObject(1).getJSONObject("data").getJSONArray("children");
				for (Object obj : json) {
					JSONObject item = (JSONObject) obj;
					String author = item.getJSONObject("data").getString("author");
					String ign = item.getJSONObject("data").getString("body");
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
					ChatPlayer mp = ChatPlayer.GetFromName(ign);
					if (mp == null)
						continue;
					if (!JoinedBefore(mp, 2015, 6, 5))
						continue;
					if (!mp.UserNames.contains(author))
						mp.UserNames.add(author);
					if (mp.FlairState.equals(FlairStates.NoComment)) {
						mp.FlairState = FlairStates.Commented;
						ConfirmUserMessage(mp);
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
		String[] flairdata = TBMCCoreAPI.DownloadString("http://karmadecay.com/thebutton-data.php?users=" + mp.UserName)
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
		SetFlair(mp, flair, flairclass, mp.UserName);
	}

	public static Exception LastException;

	private void SetFlair(ChatPlayer p, String text, String flairclass, String username) {
		p.UserName = username;
		p.FlairState = FlairStates.Recognised;
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
					p.FlairState = FlairStates.Commented; // Flair unknown
					p.SetFlair(ChatPlayer.FlairTimeNone);
					e.printStackTrace();
				}
			} else {
				try {
					if (CheckForJoinDate(p)) {
						p.FlairState = FlairStates.Commented; // Flair unknown
						p.SetFlair(ChatPlayer.FlairTimeNone);
					} else {
						p.SetFlair(ChatPlayer.FlairTimeCantPress);
					}
				} catch (Exception e) {
					p.FlairState = FlairStates.Commented; // Flair unknown
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
		URL url = new URL("https://www.reddit.com/u/" + mp.UserName);
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
		Player p = Bukkit.getPlayer(mp.UUID);
		if (mp.FlairState.equals(FlairStates.Commented) && p != null)
			if (mp.UserNames.size() > 1)
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

	public static void LoadFiles(boolean reload) {
		if (reload) {
			PluginMain.Instance.getLogger().info("Cleanup for reloading...");
			ChatPlayer.OnlinePlayers.clear();
			AnnounceMessages.clear();
		}
		PluginMain.Instance.getLogger().info("Loading files...");
		try {
			File file = new File("thebuttonmc.yml"); // TODO
			if (file.exists()) {
				YamlConfiguration yc = new YamlConfiguration();
				yc.load(file);
				PlayerListener.NotificationSound = yc.getString("notificationsound");
				PlayerListener.NotificationPitch = yc.getDouble("notificationpitch");
				AnnounceTime = yc.getInt("announcetime"); // TODO: Move out to
															// the core
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
			File file = new File("thebuttonmc.yml");
			YamlConfiguration yc = new YamlConfiguration();
			yc.set("notificationsound", PlayerListener.NotificationSound);
			yc.set("notificationpitch", PlayerListener.NotificationPitch);
			yc.set("announcetime", AnnounceTime); // TODO: Move out to the core
			yc.set("announcements", AnnounceMessages);
			yc.set("alphadeaths", PlayerListener.AlphaDeaths);
			yc.save(file);
			PluginMain.Instance.getLogger().info("Saved files!");
		} catch (IOException e) {
			PluginMain.Instance.getLogger().warning("Error!\n" + e);
			LastException = e;
		}
	}

	private void addClassPath(final URL url) throws IOException {
		final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		final Class<URLClassLoader> sysclass = URLClassLoader.class;
		try {
			final Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { url });
		} catch (final Throwable t) {
			t.printStackTrace();
			throw new IOException("Error adding " + url + " to system classloader");
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
