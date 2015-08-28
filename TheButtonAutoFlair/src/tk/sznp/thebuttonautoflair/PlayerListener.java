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

		File f = new File("plugins/essentials/userdata/" + id + ".yml");
		YamlConfiguration yc = new YamlConfiguration();
		try {
			yc.load(f);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		String nickname = yc.getString("nickname");
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

		mp.RPMode = true; // 2015.08.25.
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
			String nwithoutformatting = n;
			int index;
			// System.out.println("n: " + n);
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				// if ((index = nwithoutformatting.indexOf('§')) != -1)
				// {
				nwithoutformatting = nwithoutformatting.replaceAll("§"
						+ nwithoutformatting.charAt(index + 1), "");
			// System.out.println("Index: "+index+" "+"CharAt(index+1): "+nwithoutformatting.charAt(index+1));
			// }
			// System.out.println("nwithoutformatting: " + nwithoutformatting);
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
				message = message.replaceAll(nwithoutformatting, n);
			}
		}

		event.setFormat("<"
				+ (player.RPMode ? "§2[RP]§r" : "§8[non-RP]§r")
				+ event.getFormat().substring(
						event.getFormat().indexOf("<") + 1,
						event.getFormat().indexOf(">")) + flair + "> "
				+ message); // 2015.08.08.
	}

	/*
	 * private static Class<?> nmsChatSerializer = Reflection
	 * .getNMSClass("IChatBaseComponent$ChatSerializer"); private static
	 * Class<?> nmsPacketPlayOutChat = Reflection
	 * .getNMSClass("PacketPlayOutChat");
	 * 
	 * public static void sendRawMessage(Player player, String message) { try {
	 * Object handle = Reflection.getHandle(player); Object connection =
	 * Reflection.getField(handle.getClass(), "playerConnection").get(handle);
	 * Object serialized = Reflection.getMethod(nmsChatSerializer, "a",
	 * String.class).invoke(null, message); Object packet =
	 * nmsPacketPlayOutChat.getConstructor(
	 * Reflection.getNMSClass("IChatBaseComponent")).newInstance( serialized);
	 * Reflection.getMethod(connection.getClass(), "sendPacket").invoke(
	 * connection, packet); } catch (Exception e) { e.printStackTrace();
	 * PluginMain.LastException = e; // 2015.08.09. } }
	 */

	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent e) {
		String name = e.getLastToken();
		for (String nickname : nicknames.keySet()) {
			String nwithoutformatting = nickname;
			int index;
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replaceAll("§"
						+ nwithoutformatting.charAt(index + 1), "");
			if (nwithoutformatting.startsWith(name))
				e.getTabCompletions().add(nwithoutformatting);
		}

	}
}
