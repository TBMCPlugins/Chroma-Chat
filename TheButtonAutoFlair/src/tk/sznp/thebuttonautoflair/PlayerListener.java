package tk.sznp.thebuttonautoflair;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PlayerListener implements Listener { // 2015.07.16.
	public static HashMap<String, UUID> nicknames = new HashMap<>();

	public static boolean Enable = false; // 2015.08.29.

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p
				.getUniqueId());
		mp.PlayerName = p.getName(); // 2015.10.17. 0:58
		if (!mp.FlairState.equals(FlairStates.NoComment))
			// if (false)
			PluginMain.ConfirmUserMessage(mp); // 2015.08.09.
		else { // 2015.07.20.
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
					if (mp.FlairState.equals(FlairStates.NoComment)) {
						String json = "[\"\",{\"text\":\"If you'd like your /r/TheButton flair displayed ingame, write your Minecraft name to \",\"color\":\"aqua\"},{\"text\":\"[this thread].\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.reddit.com/r/TheButtonMinecraft/comments/3d25do/\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread\",\"color\":\"aqua\"}]}}}]";
						PluginMain.Instance.getServer().dispatchCommand(
								PluginMain.Console,
								"tellraw " + mp.PlayerName + " " + json);
						json = "[\"\",{\"text\":\"If you don't want the flair, type /u ignore to prevent this message after next login.\",\"color\":\"aqua\"}]";
						PluginMain.Instance.getServer().dispatchCommand(
								PluginMain.Console,
								"tellraw " + mp.PlayerName + " " + json);
					}
				}
			};
			tt.mp = mp;
			timer.schedule(tt, 15 * 1000);
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

		mp.SetFlairColor(mp.GetFlairColor()); //Update display
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
	public static double NotificationPitch; // 2015.08.14.

	public static boolean ShowRPTag = false; // 2015.08.31.

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		MaybeOfflinePlayer player = MaybeOfflinePlayer.AllPlayers.get(event
				.getPlayer().getUniqueId());
		String flair = player.GetFormattedFlair();
		String message = event.getMessage(); // 2015.08.08.
		for (Player p : PluginMain.GetPlayers()) { // 2015.08.12.
			String color = ""; // 2015.08.17.
			if (message.contains(p.getName())) {
				if (NotificationSound == null)
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f); // 2015.08.12.
				else
					p.playSound(p.getLocation(), NotificationSound, 1.0f,
							(float) NotificationPitch); // 2015.08.14.
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p
						.getUniqueId()); // 2015.08.17.
				color = String.format("§%x", (mp.GetFlairColor() == 0x00 ? 0xb
						: mp.GetFlairColor())); // TODO: Quiz queue
			}

			message = message.replace(p.getName(), color + p.getName()
					+ (event.getMessage().startsWith("§2>") ? "§2" : "§r"));
		}
		for (String n : nicknames.keySet()) {
			Player p = null;
			String nwithoutformatting = new String(n);
			int index;
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replace("§k"
						+ nwithoutformatting.charAt(index + 2), ""); // Support
																		// for
																		// one
																		// random
																		// char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replace("§"
						+ nwithoutformatting.charAt(index + 1), "");
			if (message.contains(nwithoutformatting)) {
				p = Bukkit.getPlayer(nicknames.get(n));
				if (NotificationSound == null)
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 0.5f); // 2015.08.12.
				else
					p.playSound(p.getLocation(), NotificationSound, 1.0f,
							(float) NotificationPitch); // 2015.08.14.
				MaybeOfflinePlayer.AddPlayerIfNeeded(p.getUniqueId()); // 2015.08.17.
			}
			if (p != null) {
				message = message.replace(nwithoutformatting, n
						+ (event.getMessage().startsWith("§2>") ? "§2" : "§r"));
			}
		}

		event.setMessage(message); // 2015.09.05.

		event.setFormat(event
				.getFormat()
				.replace(
						"{rptag}",
						(player.RPMode ? (ShowRPTag ? "§2[RP]§r" : "")
								: "§8[OOC]§r"))
				.replace("{buttonflair}", flair)
				.replace(
						"{isitwilds}",
						(event.getPlayer().getWorld().getName()
								.equalsIgnoreCase("wilds") ? "[PVP]" : ""))); // 2015.09.04.
	}

	@EventHandler
	public void onTabComplete(PlayerChatTabCompleteEvent e) {
		String name = e.getLastToken();
		for (String nickname : nicknames.keySet()) {
			String nwithoutformatting = nickname;
			int index;
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replace("§k"
						+ nwithoutformatting.charAt(index + 2), ""); // Support
																		// for
																		// one
																		// random
																		// char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replace("§"
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
	private int FCount = 0;

	@EventHandler
	public void onPlayerMessage(AsyncPlayerChatEvent e) {
		if (e.getMessage().equalsIgnoreCase("F")) {
			MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(e
					.getPlayer().getUniqueId());
			if (!mp.PressedF && ActiveF) {
				FCount++;
				mp.PressedF = true;
			}
		}

		if (e.getMessage().startsWith(">"))
			e.setMessage("§2" + e.getMessage());

		if (e.getMessage().contains("lol"))
			Commands.Lastlol = MaybeOfflinePlayer.AllPlayers.get(e.getPlayer()
					.getUniqueId());

		if (e.getFormat().contains("[g]")) {
			StringBuilder sb = new StringBuilder();
			sb.append("tellraw @a [\"\"");
			sb.append(",{\"text\":\"Hashtags:\"}");
			int index = -1;
			ArrayList<String> list = new ArrayList<String>();
			while ((index = e.getMessage().indexOf("#", index + 1)) != -1) {
				int index2 = e.getMessage().indexOf(" ", index + 1);
				if (index2 == -1)
					index2 = e.getMessage().length();
				int index3 = e.getMessage().indexOf("#", index + 1);
				if (index3 != -1 && index3 < index2) // A # occurs before a
														// space
					index2 = index3;
				String original = e.getMessage().substring(index, index2);
				list.add(original);
				sb.append(",{\"text\":\" \"}");
				sb.append(",{\"text\":\"");
				sb.append(original);
				sb.append("\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://twitter.com/hashtag/");
				sb.append(original.substring(1));
				sb.append("\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open on Twitter\",\"color\":\"blue\"}]}}}");
			}
			for (String original : list)
				e.setMessage(e.getMessage().replace(
						original,
						"§9"
								+ original
								+ (e.getMessage().startsWith("§2>") ? "§2"
										: "§r")));
			/*
			 * for (String original : list)
			 * System.out.println(e.getMessage().replace( original, "§9" +
			 * original + (e.getMessage().startsWith("§2>") ? "§2" : "§r")));
			 */

			sb.append("]");

			if (list.size() > 0)
				PluginMain.Instance.getServer().dispatchCommand(
						PluginMain.Instance.getServer().getConsoleSender(),
						sb.toString());
		}
	}

	private Timer Ftimer;

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (!Minigames.plugin.pdata.getMinigamePlayer(e.getEntity())
				.isInMinigame() && new Random().nextBoolean()) {
			if (Ftimer != null)
				Ftimer.cancel();
			ActiveF = true;
			FCount = 0;
			for (Player p : PluginMain.GetPlayers()) {
				MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(p
						.getUniqueId());
				mp.PressedF = false;
				p.sendMessage("§bPress F to pay respects.§r");
			}
			Ftimer = new Timer();
			TimerTask tt = new TimerTask() {
				@Override
				public void run() {
					if (ActiveF) {
						ActiveF = false;
						for (Player p : PluginMain.GetPlayers()) {
							p.sendMessage("§b" + FCount + " "
									+ (FCount == 1 ? "person" : "people")
									+ " paid their respects.§r");
						}
					}
				}
			};
			Ftimer.schedule(tt, 15 * 1000);
		}
	}

	@EventHandler
	public void onPlayerItemPickup(PlayerPickupItemEvent e) {
		// System.out.println("A");
		MinigamePlayer mp = Minigames.plugin.pdata.getMinigamePlayer(e
				.getPlayer());
		// System.out.println("B");

		/*
		 * if (!e.getPlayer().isOp() && (!mp.isInMinigame() ||
		 * mp.getMinigame().getName(false)
		 * .equalsIgnoreCase(Commands.KittyCannonMinigame))) return;
		 */
		if (!(mp.isInMinigame() && mp.getMinigame().getName(false)
				.equalsIgnoreCase(Commands.KittyCannonMinigame)))
			return;
		// System.out.println("C");
		ItemStack item = e.getItem().getItemStack();
		if (!item.getType().equals(Material.SKULL_ITEM)
				&& !item.getType().equals(Material.SKULL))
			return;
		// System.out.println("D");
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		if (!meta.getDisplayName().equals("§rOcelot Head")
				|| !meta.getOwner().equals("MHF_Ocelot"))
			return;
		// System.out.println("E");
		if (meta.getLore() == null || meta.getLore().size() == 0)
			return;
		// System.out.println("F");
		ItemStack hat = e.getPlayer().getInventory().getHelmet();
		if (!(hat != null
				&& (hat.getType().equals(Material.SKULL) || hat.getType()
						.equals(Material.SKULL_ITEM)) && ((SkullMeta) hat
					.getItemMeta()).getDisplayName().equals("§rWolf Head")))
			e.getPlayer().damage(1f * item.getAmount(),
					Bukkit.getPlayer(meta.getLore().get(0)));
		e.getItem().remove();
		// System.out.println("G");
		e.setCancelled(true);
		// System.out.println("H");
	}
}
