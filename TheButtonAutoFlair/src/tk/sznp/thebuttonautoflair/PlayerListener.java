package tk.sznp.thebuttonautoflair;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener { // 2015.07.16.
	public static HashMap<String, UUID> nicknames = new HashMap<>();

	public static boolean Enable = false; // 2015.08.29.

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		MaybeOfflinePlayer.AddPlayerIfNeeded(p.getName()); // 2015.08.08.
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(p.getName()); // 2015.08.08.
		if (mp.CommentedOnReddit)
			PluginMain.AppendPlayerDisplayFlair(mp, p); // 2015.08.09.
		else { // 2015.07.20.
				// String json =
				// "[\"\",{\"text\":\"§6Hi! If you'd like your flair displayed ingame, write your §6Minecraft name to \"},{\"text\":\"[this thread.]\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.reddit.com/r/TheButtonMinecraft/comments/3d25do/\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread§r\"}]}}}]";
				// sendRawMessage(p, json);
			if (!mp.IgnoredFlair) {
				String message = "§bHi! If you'd like your flair displayed ingame, write your §6Minecraft name to this thread:§r";
				p.sendMessage(message);
				message = "§bhttps://www.reddit.com/r/TheButtonMinecraft/comments/3d25do/§r";
				p.sendMessage(message);
				message = "§bIf you don't want the flair, type /u ignore to prevent this message on login.§r";
				p.sendMessage(message);
			}
		}

		/* NICKNAME LOGIC */

		UUID id = p.getUniqueId();

		File f = new File("plugins/Essentials/userdata/" + id + ".yml");
		if (f.exists()) {
			YamlConfiguration yc = new YamlConfiguration();
			try {
				yc.load(f);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			String nickname = yc.getString("nickname");
			if (nickname != null) {
				nicknames.put(nickname, id);

				if (Enable) {
					if (!p.getName().equals("NorbiPeti")) {
						for (Player player : PluginMain.GetPlayers()) {
							if (player.getName().equals("NorbiPeti")) {
								player.chat("Hey, " + nickname + "!");
								break;
							}
						}
					}
				}
			}
		}
		/*
		 * System.out.println("Folder plugins exists: "+new
		 * File("plugins").isDirectory());
		 * System.out.println("Folder plugins/essentials exists: "+new
		 * File("plugins/essentials").isDirectory());
		 * System.out.println("Folder plugins/essentials/userdata exists: "+new
		 * File("plugins/essentials/userdata").isDirectory());
		 * System.out.println
		 * ("Folder plugins/essentials/userdata/"+id+".yml exists: "+new
		 * File("plugins/essentials/userdata/"+id+".yml").exists());
		 */

		SendForDebug("Folder plugins exists: "
				+ new File("plugins").isDirectory());
		SendForDebug("Folder plugins/Essentials exists: "
				+ new File("plugins/Essentials").isDirectory());
		SendForDebug("Folder plugins/Essentials/userdata exists: "
				+ new File("plugins/Essentials/userdata").isDirectory());
		SendForDebug("Folder plugins/Essentials/userdata/"
				+ id
				+ ".yml exists: "
				+ new File("plugins/Essentials/userdata/" + id + ".yml")
						.exists());

		mp.RPMode = true; // 2015.08.25.

		if (p.getName().equals("FigyTuna") && Commands.PluginUpdated)
			p.sendMessage("§bThe The Button MC plugin got updated. Please restart the server. :P§r");
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		String deletenick = null;
		for (String nickname : nicknames.keySet()) {
			UUID uuid = nicknames.get(nickname);
			if (event.getPlayer().getUniqueId().equals(uuid)) {
				deletenick = nickname;
				break;
			}
		}
		if (deletenick != null)
			nicknames.remove(deletenick);
	}

	public static String NotificationSound; // 2015.08.14.
	public static float NotificationPitch; // 2015.08.14.

	public static boolean ShowRPTag = false; // 2015.08.31.

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		MaybeOfflinePlayer player = MaybeOfflinePlayer.AllPlayers.get(event
				.getPlayer().getName());
		String flair = player.Flair; // 2015.08.08.
		if (player.IgnoredFlair)
			flair = "";
		String message = event.getMessage(); // 2015.08.08.
		for (Player p : PluginMain.GetPlayers()) { // 2015.08.12.
			String color = ""; // 2015.08.17.
			if (message.contains(p.getName())) {
				ArrayList<Integer> NamePositions = new ArrayList<>();
				for (String n : nicknames.keySet()) {
					String nwithoutformatting = new String(n);
					int index;
					while ((index = nwithoutformatting.indexOf("§k")) != -1)
						nwithoutformatting = nwithoutformatting.replaceAll("§k"
								+ nwithoutformatting.charAt(index + 2), ""); // Support
																				// for
																				// one
																				// random
																				// char
					while ((index = nwithoutformatting.indexOf('§')) != -1)
						nwithoutformatting = nwithoutformatting.replaceAll("§"
								+ nwithoutformatting.charAt(index + 1), "");
					/*
					 * if (nwithoutformatting.contains(p.getName())) { HasName =
					 * true; break; }
					 */
				}
				// if (!HasName) {
				if (NotificationSound == null)
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f); // 2015.08.12.
				else
					p.playSound(p.getLocation(), NotificationSound, 1.0f,
							NotificationPitch); // 2015.08.14.
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p
						.getName()); // 2015.08.17.
				if (mp.Flair.length() > 1)
					color = mp.Flair.substring(0, 2);
				// }
			}

			// if (!HasName)
			message = message.replaceAll(p.getName(), color + p.getName()
					+ "§r");
		}
		for (String n : nicknames.keySet()) {
			Player p = null;
			//event.getPlayer().sendMessage("n before: " + n); // TMP
			String nwithoutformatting = new String(n);
			int index;
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("§k"
						+ nwithoutformatting.charAt(index + 2), ""); // Support
																		// for
																		// one
																		// random
																		// char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("§"
						+ nwithoutformatting.charAt(index + 1), "");
			//event.getPlayer().sendMessage(nwithoutformatting); // TMP
			if (message.contains(nwithoutformatting)) {
				//event.getPlayer().sendMessage("Yep"); // TMP
				//event.getPlayer().sendMessage(n); // TMP
				p = Bukkit.getPlayer(nicknames.get(n));
				if (NotificationSound == null)
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f); // 2015.08.12.
				else
					p.playSound(p.getLocation(), NotificationSound, 1.0f,
							NotificationPitch); // 2015.08.14.
				MaybeOfflinePlayer.AddPlayerIfNeeded(p.getName()); // 2015.08.17.
			}
			if (p != null) {
				message = message.replaceAll(nwithoutformatting, n + "§r");
			}
		}

		event.setMessage(message); // 2015.09.05.

		event.setFormat(event
				.getFormat()
				.replace(
						"{rptag}",
						(player.RPMode ? (ShowRPTag ? "§2[RP]§r" : "")
								: "§8[OOC]§r")).replace("{buttonflair}", flair)); // 2015.09.04.
	}

	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent e) {
		String name = e.getLastToken();
		for (String nickname : nicknames.keySet()) {
			String nwithoutformatting = nickname;
			int index;
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("§k"
						+ nwithoutformatting.charAt(index + 2), ""); // Support
																		// for
																		// one
																		// random
																		// char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("§"
						+ nwithoutformatting.charAt(index + 1), "");
			if (nwithoutformatting.startsWith(name))
				e.getTabCompletions().add(nwithoutformatting);
		}
	}

	public static boolean DebugMode = false;

	public void SendForDebug(String message) {
		if (DebugMode) {
			for (Player player : PluginMain.GetPlayers()) {
				if (player.getName().equals("NorbiPeti")) {
					player.sendMessage("[DEBUG] " + message);
					break;
				}
			}
		}
	}


	@EventHandler
	public void onPlayerMessage(AsyncPlayerChatEvent e)
	{
		if (e.getMessage().startsWith(">"))
            e.setMessage("§2"+e.getMessage());
	}
}
