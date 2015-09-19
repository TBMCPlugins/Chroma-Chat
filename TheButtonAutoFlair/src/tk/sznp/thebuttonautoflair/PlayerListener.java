package tk.sznp.thebuttonautoflair;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
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
				}
				if (NotificationSound == null)
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f); // 2015.08.12.
				else
					p.playSound(p.getLocation(), NotificationSound, 1.0f,
							NotificationPitch); // 2015.08.14.
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p
						.getName()); // 2015.08.17.
				if (mp.Flair.length() > 1)
					color = mp.Flair.substring(0, 2);
			}

			message = message.replaceAll(p.getName(), color + p.getName()
					+ "§r");
		}
		for (String n : nicknames.keySet()) {
			Player p = null;
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
			if (message.contains(nwithoutformatting)) {
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
			if (nwithoutformatting.startsWith(name)
					&& !nwithoutformatting.equals(Bukkit.getPlayer(
							nicknames.get(nickname)).getName()))
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

	private boolean ActiveF = false;

	@EventHandler
	public void onPlayerMessage(AsyncPlayerChatEvent e) {
		if (e.getMessage().equalsIgnoreCase("F")) {
			MaybeOfflinePlayer.AllPlayers.get(e.getPlayer().getName()).PressedF = true;
			boolean F = true;
			if (ActiveF)
				for (Player player : PluginMain.GetPlayers())
					if (!MaybeOfflinePlayer.AllPlayers.get(player.getName()).PressedF)
						F = false;

			if (F && ActiveF) {
				for (Player player : PluginMain.GetPlayers()) {
					player.sendMessage("§6We did it!§r");
					MaybeOfflinePlayer.AllPlayers.get(player.getName()).PressedF = false;
				}
			}
			ActiveF = false;
		}

		if (e.getMessage().startsWith(">"))
			e.setMessage("§2" + e.getMessage());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		ActiveF = true;
		if (new Random().nextBoolean()) {
			for (Player p : PluginMain.GetPlayers()) {
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(p
						.getName());
				mp.PressedF = false;
				p.sendMessage("§bPress F to pay respects.§r");
			}
		}
	}
}
