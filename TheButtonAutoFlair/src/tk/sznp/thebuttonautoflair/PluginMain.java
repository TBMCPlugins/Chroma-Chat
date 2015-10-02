package tk.sznp.thebuttonautoflair;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.apache.commons.io.IOUtils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PluginMain extends JavaPlugin { // Translated to Java: 2015.07.15.
	// A user, which flair isn't obtainable:
	// https://www.reddit.com/r/thebutton/comments/31c32v/i_pressed_the_button_without_really_thinking/
	public static PluginMain Instance;
	public static ConsoleCommandSender Console; // 2015.08.12.

	// Fired when plugin is first enabled
	@Override
	public void onEnable() {
		System.out.println("The Button Minecraft server plugin");
		getServer().getPluginManager().registerEvents(new PlayerListener(),
				this);
		Commands comm = new Commands();
		this.getCommand("u").setExecutor(comm);
		this.getCommand("u").setUsage(
				this.getCommand("u").getUsage().replace('&', '§'));
		this.getCommand("nrp").setExecutor(comm);
		this.getCommand("nrp").setUsage(
				this.getCommand("nrp").getUsage().replace('&', '§'));
		this.getCommand("ooc").setExecutor(comm);
		this.getCommand("ooc").setUsage(
				this.getCommand("ooc").getUsage().replace('&', '§'));
		Instance = this; // 2015.08.08.
		Console = this.getServer().getConsoleSender(); // 2015.08.12.
		LoadFiles(false); // 2015.08.09.
		Runnable r = new Runnable() {
			public void run() {
				ThreadMethod();
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

	public void ThreadMethod() // <-- 2015.07.16.
	{
		while (!stop) {
			try {
				String body = DownloadString("https://www.reddit.com/r/TheButtonMinecraft/comments/3d25do/autoflair_system_comment_your_minecraft_name_and/.json?limit=1000");
				JSONArray json = new JSONArray(body).getJSONObject(1)
						.getJSONObject("data").getJSONArray("children");
				for (Object obj : json) {
					JSONObject item = (JSONObject) obj;
					String author = item.getJSONObject("data").getString(
							"author");
					String ign = item.getJSONObject("data").getString("body");
					int start = ign.indexOf("IGN:") + "IGN:".length();
					if (start == -1 + "IGN:".length()) // +length: 2015.08.10.
						continue; // 2015.08.09.
					int end = ign.indexOf(' ', start);
					if (end == -1 || end == start)
						end = ign.indexOf('\n', start); // 2015.07.15.
					if (end == -1 || end == start)
						ign = ign.substring(start);
					else
						ign = ign.substring(start, end);
					ign = ign.trim();
					if (HasIGFlair(ign))
						continue;
					try {
						Thread.sleep(10);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					String[] flairdata = DownloadString(
							"http://karmadecay.com/thebutton-data.php?users="
									+ author).replace("\"", "").split(":");
					String flair;
					if (flairdata.length > 1) // 2015.07.15.
						flair = flairdata[1];
					else
						flair = "";
					if (flair != "-1")
						flair = flair + "s";
					String flairclass;
					if (flairdata.length > 2)
						flairclass = flairdata[2];
					else
						flairclass = "unknown";
					SetFlair(ign, flair, flairclass, author);
				}
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			} catch (Exception e) {
				//System.out.println("Error!\n" + e);
				LastException = e; // 2015.08.09.
			}
		}
	}

	public static Exception LastException; // 2015.08.09.

	public String DownloadString(String urlstr) throws MalformedURLException,
			IOException {
		URL url = new URL(urlstr);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "TheButtonAutoFlair");
		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		in.close();
		return body;
	}

	public static Map<String, String> TownColors = new HashMap<String, String>(); // 2015.07.20.

	public Boolean HasIGFlair(String playername) {
		MaybeOfflinePlayer p = MaybeOfflinePlayer.AddPlayerIfNeeded(playername); // 2015.08.08.
		return p.CommentedOnReddit; // 2015.08.10.
	}

	public void SetFlair(String playername, String text, String flairclass,
			String username) {
		MaybeOfflinePlayer p = MaybeOfflinePlayer.AddPlayerIfNeeded(playername); // 2015.08.08.
		String finalflair;
		p.FlairDecided = true;
		p.FlairRecognised = true;
		switch (flairclass) {
		case "press-1":
			finalflair = "§c(" + text + ")§r";
			break;
		case "press-2":
			finalflair = "§6(" + text + ")§r";
			break;
		case "press-3":
			finalflair = "§e(" + text + ")§r";
			break;
		case "press-4":
			finalflair = "§a(" + text + ")§r";
			break;
		case "press-5":
			finalflair = "§9(" + text + ")§r";
			break;
		case "press-6":
			finalflair = "§5(" + text + ")§r";
			break;
		case "no-press":
			finalflair = "§7(--s)§r";
			break;
		case "cheater":
			finalflair = "§5(" + text + ")§r";
			break;
		case "cant-press": // 2015.08.08.
			finalflair = "§r(??s)§r";
			break;
		case "unknown":
			if (text.equals("-1")) // If true, only non-presser/can't press; if
									// false, any flair
				p.FlairDecided = false;
			else
				p.FlairRecognised = false;
			finalflair = "";
			break;
		default:
			return;
		}
		p.Flair = finalflair; // 2015.08.08.
		p.CommentedOnReddit = true; // 2015.08.10.
		p.UserName = username; // 2015.08.08.
		for (Player player : getServer().getOnlinePlayers()) // <-- 2015.08.08.
		{
			if (player.getName().equals(playername)) {
				AppendPlayerDisplayFlair(p, player);
				break;
			}
		}
	}

	public static String GetFlair(Player player) { // 2015.07.16.
		String flair = MaybeOfflinePlayer.AllPlayers.get(player.getName()).Flair; // 2015.08.08.
		// return flair==null ? "" : flair;
		return flair; // 2015.08.10.
	}

	public static void AppendPlayerDisplayFlair(MaybeOfflinePlayer player,
			Player p) // <-- 2015.08.09.
	{

		if (MaybeOfflinePlayer.AllPlayers.get(p.getName()).IgnoredFlair)
			return;
		if (MaybeOfflinePlayer.AllPlayers.get(p.getName()).AcceptedFlair) {
			if (!player.FlairDecided)
				p.sendMessage("§9Your flair type is unknown. Are you a non-presser or a can't press? (/u nonpresser or /u cantpress)§r"); // 2015.08.09.
		} else
			p.sendMessage("§9Are you Reddit user " + player.UserName
					+ "?§r §6Type /u accept or /u ignore§r");
	}

	public static String GetColorForTown(String townname) { // 2015.07.20.
		if (TownColors.containsKey(townname))
			return TownColors.get(townname);
		return "";
	}

	public static String GetPlayerTown(Player player) { // 2015.07.20.
		try {
			Town town = WorldCoord.parseWorldCoord(player).getTownBlock()
					.getTown(); // TODO
			return town.getName();
		} catch (Exception e) {
			return "";
		}
	}

	public static Collection<? extends Player> GetPlayers() {
		return Instance.getServer().getOnlinePlayers();
	}

	public static ArrayList<String> AnnounceMessages = new ArrayList<>();
	public static int AnnounceTime = 15 * 60 * 1000;

	public static void LoadFiles(boolean reload) // <-- 2015.08.09.
	{
		if (reload) { // 2015.08.09.
			System.out
					.println("The Button Minecraft plugin cleanup for reloading...");
			MaybeOfflinePlayer.AllPlayers.clear();
			TownColors.clear();
			AnnounceMessages.clear();
		}
		System.out.println("Loading files for The Button Minecraft plugin..."); // 2015.08.09.
		try {
			File file = new File("flairsaccepted.txt");
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(
						"flairsaccepted.txt"));
				String line;
				while ((line = br.readLine()) != null) {
					String name = line.replace("\n", "");
					// System.out.println("Name: " + name);
					MaybeOfflinePlayer.AddPlayerIfNeeded(name).AcceptedFlair = true; // 2015.08.08.
				}
				br.close();
			}
			file = new File("flairsignored.txt");
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(
						"flairsignored.txt"));
				String line;
				while ((line = br.readLine()) != null) {
					String name = line.replace("\n", "");
					MaybeOfflinePlayer.AddPlayerIfNeeded(name).IgnoredFlair = true; // 2015.08.08.
				}
				br.close();
			}
			file = new File("autoflairconfig.txt");
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					String[] s = line.split(" ");
					if (s.length >= 2) // <-- 2015.08.10.
						TownColors.put(s[0], s[1]);
				}
				br.close();
			}
			file = new File("customflairs.txt"); // 2015.08.09.
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					String[] s = line.split(" ");
					if (s.length >= 2) // 2015.08.10.
					{
						MaybeOfflinePlayer p = MaybeOfflinePlayer
								.AddPlayerIfNeeded(s[0]);
						p.Flair = s[1]; // 2015.08.09.
						p.CommentedOnReddit = true; // Kind of
						p.FlairDecided = true;
						p.FlairRecognised = true;
					}
				}
				br.close();
			}
			file = new File("notificationsound.txt"); // 2015.08.09.
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				String[] split = line.split(" ");
				PlayerListener.NotificationSound = split[0];
				PlayerListener.NotificationPitch = Float.parseFloat(split[1]);
				br.close();
			}
			file = new File("announcemessages.txt"); // 2015.08.09.
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line;
				boolean first = true;
				while ((line = br.readLine()) != null) {
					if (first) {
						AnnounceTime = Integer.parseInt(line.trim());
						first = false;
					} else
						AnnounceMessages.add(line.trim());
				}
				br.close();
			} else {
				// Write time
				try {
					BufferedWriter bw;
					bw = new BufferedWriter(new FileWriter(file));
					bw.write(AnnounceTime + "\n");
					bw.close();
				} catch (IOException e) {
					System.out.println("Error!\n" + e);
					PluginMain.LastException = e; // 2015.08.09.
				}
			}
			System.out.println("The Button Minecraft plugin loaded files!");
		} catch (IOException e) {
			System.out.println("Error!\n" + e);
			LastException = e; // 2015.08.09.
		}
	}

	public static void SaveFiles() // <-- 2015.08.09.
	{
		try {
			FileWriter fw;
			fw = new FileWriter("flairsaccepted.txt");
			fw.close();
			fw = new FileWriter("flairsignored.txt");
			fw.close();
		} catch (Exception e) {
			System.out.println("Error!\n" + e);
			LastException = e; // 2015.08.09.
		}
		try {
			File file = new File("flairsaccepted.txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			for (MaybeOfflinePlayer player : MaybeOfflinePlayer.AllPlayers
					.values()) // <-- 2015.08.08.
			{
				if (!player.AcceptedFlair)
					continue; // 2015.08.08.
				bw.write(player.PlayerName + "\n");
			}
			bw.close();
			file = new File("flairsignored.txt");
			bw = new BufferedWriter(new FileWriter(file, true));
			for (MaybeOfflinePlayer player : MaybeOfflinePlayer.AllPlayers
					.values()) // <-- 2015.08.08.
			{
				if (!player.IgnoredFlair)
					continue; // 2015.08.08.
				bw.write(player.PlayerName + "\n");
			}
			bw.close();
		} catch (IOException e) {
			System.out.println("Error!\n" + e);
			LastException = e; // 2015.08.09.
		}
	}

	public static boolean RemoveLineFromFile(String file, String line) { // 2015.08.09.
		File inputFile = new File(file);
		File tempFile = new File("_temp.txt");

		if (!inputFile.exists())
			return true; // 2015.08.10.

		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String lineToRemove = line;
			String currentLine;

			while ((currentLine = reader.readLine()) != null) {
				// trim newline when comparing with lineToRemove
				String trimmedLine = currentLine.trim();
				if (trimmedLine.split(" ")[0].equals(lineToRemove))
					continue; // 2015.08.17.
				writer.write(currentLine + System.getProperty("line.separator"));
			}
			writer.close();
			reader.close();
			if (!tempFile.renameTo(inputFile)) {
				inputFile.delete();
				return tempFile.renameTo(inputFile);
			} else
				return true;
		} catch (IOException e) {
			System.out.println("Error!\n" + e);
			LastException = e; // 2015.08.09.
		}
		return false;
	}

	public static boolean RemoveLineFromFile(String file, int index) {
		File inputFile = new File(file);
		File tempFile = new File("_temp2.txt");

		if (!inputFile.exists())
			return true; // 2015.08.10.

		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

			String currentLine;
			int i = 0;

			while ((currentLine = reader.readLine()) != null) {
				if (i++ == index)
					continue;
				writer.write(currentLine + System.getProperty("line.separator"));
			}
			writer.close();
			reader.close();
			if (!tempFile.renameTo(inputFile)) {
				inputFile.delete();
				return tempFile.renameTo(inputFile);
			} else
				return true;
		} catch (IOException e) {
			System.out.println("Error!\n" + e);
			LastException = e; // 2015.08.09.
		}
		return false;
	}
}
