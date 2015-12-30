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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Objective;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

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
import java.util.regex.Pattern;

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

		mp.SetFlairColor(mp.GetFlairColor()); // Update display
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
		if (event.getMessage().equalsIgnoreCase("F")) {
			MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(event
					.getPlayer().getUniqueId());
			if (!mp.PressedF && ActiveF) {
				FCount++;
				mp.PressedF = true;
				if (FPlayer != null && FPlayer.FCount < Integer.MAX_VALUE - 1)
					FPlayer.FCount++;
			}
		}

		boolean greentext = event.getMessage().startsWith(">");
		Commands.Lastlol = MaybeOfflinePlayer.AllPlayers.get(event.getPlayer()
				.getUniqueId());

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

			message = message.replaceAll("(?i)" + Pattern.quote(p.getName()),
					color
							+ p.getName()
							+ (event.getMessage().startsWith("§2>") ? "§2"
									: "§r"));
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
				message = message.replaceAll(
						"(?i)" + Pattern.quote(nwithoutformatting), n
								+ (event.getMessage().startsWith("§2>") ? "§2"
										: "§r"));
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

		event.setCancelled(true);
		StringBuilder json = new StringBuilder();
		json.append("[\"\",");
		json.append(String.format("{\"text\":\"[%s]%s <\"},",
				player.CurrentChannel.DisplayName, (!player.RPMode ? "[OOC]"
						: "")));
		json.append(String.format("{\"text\":\"%s%s\",", event.getPlayer()
				.getDisplayName(), player.GetFormattedFlair()));
		json.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[");
		json.append(String.format("{\"text\":\"%s\n\",", event.getPlayer()
				.getName()));
		json.append(String
				.format("\"color\":\"aqua\"},{\"text\":\"World: %s\n\",\"color\":\"white\"},",
						event.getPlayer().getWorld().getName()));
		json.append(String.format(
				"{\"text\":\"Respect: %s%s\",\"color\":\"white\"}]}}},",
				(player.FCount == Integer.MAX_VALUE - 1 ? player.FCount + "+"
						: player.FCount), (player.UserName != null
						&& !player.UserName.isEmpty() ? "\nUserName: "
						+ player.UserName : "")));
		json.append("{\"text\":\"> \",\"color\":\"white\"},");

		int index = -1;
		ArrayList<String> list = new ArrayList<String>();
		while ((index = event.getMessage().indexOf("#", index + 1)) != -1) {
			int index2 = event.getMessage().indexOf(" ", index + 1);
			if (index2 == -1)
				index2 = event.getMessage().length();
			int index3 = event.getMessage().indexOf("#", index + 1);
			if (index3 != -1 && index3 < index2) // A # occurs before a
													// space
				index2 = index3;
			String original = event.getMessage().substring(index + 1, index2);
			list.add(original);
		}
		String finalstring = event.getMessage().replace('"', '\'');
		for (String original : list)
			// Hashtags
			finalstring = finalstring
					.replace(
							"#" + original,
							String.format(
									"\",\"color\":\"%s\"},{\"text\":\"#%s\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://twitter.com/hashtag/%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open on Twitter\",\"color\":\"blue\"}]}}},{\"text\":\"",
									(greentext ? "dark_green"
											: player.CurrentChannel.Color),
									original, original));
		json.append(String.format("{\"text\":\"%s\",\"color\":\"%s\"}]",
				finalstring, (greentext ? "dark_green"
						: player.CurrentChannel.Color)));
		if (!player.CurrentChannel.equals(Channel.GlobalChat))
			// for (Resident resident :
			// PluginMain.Instance.TU.getResidentMap().values()) {
			for (Player p : PluginMain.GetPlayers()) {
				try {
					Resident resident = PluginMain.Instance.TU.getResidentMap()
							.get(p.getName());
					if (!resident.getName().equals(event.getPlayer().getName())
							&& resident.getModes().contains("spy"))
						Bukkit.getPlayer(resident.getName()).sendMessage(
								String.format("[SPY-%s] - %s: %s",
										player.CurrentChannel.DisplayName,
										event.getPlayer().getDisplayName(),
										event.getMessage()));
				} catch (Exception e) {
				}
			}
		if (player.CurrentChannel.equals(Channel.TownChat)) {
			try {
				// System.out.println(PluginMain.Instance.TU.getResidentMap().keys().nextElement());
				Town town = null;
				try {
					town = PluginMain.Instance.TU.getResidentMap()
							.get(event.getPlayer().getName().toLowerCase())
							.getTown();
				} catch (NotRegisteredException e) {
				}
				if (town == null) {
					event.getPlayer().sendMessage(
							"§cYou aren't in a town or an error occured.");
					return;
				}
				index = PluginMain.Instance.Towns.indexOf(town);
				if (index < 0) {
					PluginMain.Instance.Towns.add(town);
					index = PluginMain.Instance.Towns.size() - 1;
				}
				// PluginMain.SB.getObjective("town").getScore(event.getPlayer().getName()).setScore(index);
				//System.out.println("index: " + index);
				Objective obj = PluginMain.SB.getObjective("town");
				//System.out.println("obj: " + obj);
				for (Player p : PluginMain.GetPlayers()) {
					System.out.println(town.getName());
					try {
						if (PluginMain.Instance.TU.getResidentMap()
								.get(p.getName().toLowerCase()).getTown().getName()
								.equals(town.getName()))
							obj.getScore(p.getName()).setScore(index);
						else
							obj.getScore(p.getName()).setScore(-1);
					} catch (Exception e) {
					}
				}
				PluginMain.Instance
						.getServer()
						.dispatchCommand(
								PluginMain.Console,
								String.format(
										"tellraw @a[score_town=%d,score_town_min=%d] %s",
										index, index, json.toString()));
			} catch (IllegalStateException e) {
				e.printStackTrace();
				event.getPlayer()
						.sendMessage(
								"§cAn error occured while sending the message. (IllegalStateException)");
				return;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				event.getPlayer()
						.sendMessage(
								"§cAn error occured while sending the message. (IllegalArgumentException)");
				return;
			}
		} else if (player.CurrentChannel.equals(Channel.NationChat)) {
			try {
				Town town = null;
				try {
					town = PluginMain.Instance.TU.getResidentMap()
							.get(event.getPlayer().getName().toLowerCase())
							.getTown();
				} catch (NotRegisteredException e) {
				}
				if (town == null) {
					event.getPlayer().sendMessage(
							"§cYou aren't in a town or an error occured.");
					return;
				}
				Nation nation = null;
				try {
					nation = town.getNation();
				} catch (NotRegisteredException e) {
				}
				if (nation == null) {
					event.getPlayer()
							.sendMessage(
									"§cYour town isn't in a nation or an error occured.");
					return;
				}
				index = PluginMain.Instance.Nations.indexOf(nation);
				if (index < 0) {
					PluginMain.Instance.Nations.add(nation);
					index = PluginMain.Instance.Nations.size() - 1;
				}
				// PluginMain.SB.getObjective("nation").getScore(event.getPlayer().getName()).setScore(index);
				Objective obj = PluginMain.SB.getObjective("nation");
				for (Player p : PluginMain.GetPlayers()) {
					try {
						if (PluginMain.Instance.TU.getResidentMap()
								.get(p.getName().toLowerCase()).getTown().getNation()
								.getName().equals(nation.getName()))
							obj.getScore(p.getName()).setScore(index);
					} catch (Exception e) {
					}
				}
				PluginMain.Instance
						.getServer()
						.dispatchCommand(
								PluginMain.Console,
								String.format(
										"tellraw @a[score_nation=%d,score_nation_min=%d] %s",
										index, index, json.toString()));
			} catch (IllegalStateException e) {
				e.printStackTrace();
				event.getPlayer()
						.sendMessage(
								"§cAn error occured while sending the message. (IllegalStateException)");
				return;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				event.getPlayer()
						.sendMessage(
								"§cAn error occured while sending the message. (IllegalArgumentException)");
				return;
			}
		} else
			PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
					String.format("tellraw @a %s", json.toString()));
		PluginMain.Instance
				.getServer()
				.getConsoleSender()
				.sendMessage(
						String.format("[%s] <%s%s> %s",
								player.CurrentChannel.DisplayName, event
										.getPlayer().getDisplayName(), player
										.GetFormattedFlair(), message));
	}

	@EventHandler
	public void PlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().length() < 2)
			return;
		int index = event.getMessage().indexOf(" ");
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(event
				.getPlayer().getUniqueId());
		if (index == -1) {
			String cmd = event.getMessage().substring(1);
			// System.out.println("cmd: " + cmd);
			if (cmd.equalsIgnoreCase(Channel.GlobalChat.Command)) {
				mp.CurrentChannel = Channel.GlobalChat;
				event.getPlayer().sendMessage(
						"§6You are now talking in: §b"
								+ mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			} else if (cmd.equalsIgnoreCase(Channel.TownChat.Command)) {
				if (mp.CurrentChannel.equals(Channel.TownChat))
					mp.CurrentChannel = Channel.GlobalChat;
				else
					mp.CurrentChannel = Channel.TownChat;
				event.getPlayer().sendMessage(
						"§6You are now talking in: §b"
								+ mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			} else if (cmd.equalsIgnoreCase(Channel.NationChat.Command)) {
				if (mp.CurrentChannel.equals(Channel.NationChat))
					mp.CurrentChannel = Channel.GlobalChat;
				else
					mp.CurrentChannel = Channel.NationChat;
				event.getPlayer().sendMessage(
						"§6You are now talking in: §b"
								+ mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			}
		} else {
			String cmd = event.getMessage().substring(1, index);
			// System.out.println("cmd: " + cmd);
			if (cmd.equalsIgnoreCase(Channel.GlobalChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.GlobalChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase(Channel.TownChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.TownChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase(Channel.NationChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.NationChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase("tpahere")) {
				Player player = Bukkit.getPlayer(event.getMessage().substring(
						index + 1));
				if (player != null)
					player.sendMessage("§b"
							+ event.getPlayer().getDisplayName()
							+ " §bis in this world: "
							+ event.getPlayer().getWorld().getName());
			}
		}
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
	private MaybeOfflinePlayer FPlayer = null;

	private Timer Ftimer;

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (!Minigames.plugin.pdata.getMinigamePlayer(e.getEntity())
				.isInMinigame() && new Random().nextBoolean()) {
			if (Ftimer != null)
				Ftimer.cancel();
			ActiveF = true;
			FCount = 0;
			FPlayer = MaybeOfflinePlayer.AllPlayers.get(e.getEntity()
					.getUniqueId());
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
