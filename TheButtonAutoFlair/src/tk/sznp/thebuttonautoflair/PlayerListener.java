package tk.sznp.thebuttonautoflair;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.Minigames;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Pattern;

import com.earth2me.essentials.*;

public class PlayerListener implements Listener { // 2015.07.16.
	public static HashMap<String, UUID> nicknames = new HashMap<>();

	public static boolean Enable = false; // 2015.08.29.

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (essentials == null)
			essentials = ((Essentials) Bukkit.getPluginManager().getPlugin(
					"Essentials"));
		final Player p = event.getPlayer();
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.AddPlayerIfNeeded(p
				.getUniqueId());
		mp.PlayerName = p.getName();
		if (!mp.FlairState.equals(FlairStates.NoComment)) {
			PluginMain.ConfirmUserMessage(mp);
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
					p.setPlayerListName(p.getName() + mp.GetFormattedFlair());
				}
			};
			tt.mp = mp;
			timer.schedule(tt, 1000);
		} else {
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
					if (mp.FlairState.equals(FlairStates.NoComment)) {
						String json = String.format("[\"\",{\"text\":\"If you'd like your /r/TheButton flair displayed ingame, write your Minecraft name to \",\"color\":\"aqua\"},{\"text\":\"[this thread].\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread\",\"color\":\"aqua\"}]}}}]", PluginMain.FlairThreadURL);
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

	public static String NotificationSound;
	public static double NotificationPitch;

	public static boolean ShowRPTag = false;

	private Essentials essentials = null;
	private final String[] LaughStrings = new String[] { "xd", "lel", "lawl",
			"kek", "lmao", "hue", "hah" };

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (essentials == null)
			essentials = ((Essentials) Bukkit.getPluginManager().getPlugin(
					"Essentials"));
		if (event.isCancelled()) // TODO: Change FactionChat to /tellraw
			return;
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
		String msg = event.getMessage().toLowerCase();
		if (msg.contains("lol")) {
			Commands.Lastlol = MaybeOfflinePlayer.AllPlayers.get(event
					.getPlayer().getUniqueId());
			Commands.Lastlolornot = true;
		} else {
			for (int i = 0; i < LaughStrings.length; i++) {
				if (msg.contains(LaughStrings[i])) {
					Commands.Lastlol = MaybeOfflinePlayer.AllPlayers.get(event
							.getPlayer().getUniqueId());
					Commands.Lastlolornot = false;
					break;
				}
			}
		}

		MaybeOfflinePlayer player = MaybeOfflinePlayer.AllPlayers.get(event
				.getPlayer().getUniqueId());
		String formattedmessage = event.getMessage();
		formattedmessage = formattedmessage.replace("\\", "\\\\"); // It's
																	// really
																	// important
																	// to escape
																	// the
																	// slashes
																	// first
		formattedmessage = formattedmessage.replace("\"", "\\\"");
		String suggestmsg = formattedmessage;

		// URLs
		String[] parts = formattedmessage.split("\\s+");
		boolean hadurls = false;
		for (String item : parts)
			try {
				URL url = new URL(item);
				formattedmessage = formattedmessage
						.replace(
								item,
								String.format(
										"\",\"color\":\"%s\"},{\"text\":\"%s\",\"color\":\"%s\",\"underlined\":\"true\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open URL\",\"color\":\"blue\"}]}}},{\"text\":\"",
										(greentext ? "green"
												: player.CurrentChannel.Color),
										url, (greentext ? "green"
												: player.CurrentChannel.Color),
										url));
				hadurls = true;
				// System.out.println("URL: " + url);
			} catch (MalformedURLException e) {
			}

		if (!hadurls) {
			for (Player p : PluginMain.GetPlayers()) { // 2015.08.12.
				String color = ""; // 2015.08.17.
				if (formattedmessage.matches("(?i).*"
						+ Pattern.quote(p.getName()) + ".*")) {
					if (NotificationSound == null)
						p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f,
								0.5f); // 2015.08.12.
					else
						p.playSound(p.getLocation(), NotificationSound, 1.0f,
								(float) NotificationPitch); // 2015.08.14.
					MaybeOfflinePlayer mp = MaybeOfflinePlayer
							.AddPlayerIfNeeded(p.getUniqueId());
					color = String.format(
							"§%x",
							(mp.GetFlairColor() == 0x00 ? 0xb : mp
									.GetFlairColor()));
				}

				formattedmessage = formattedmessage.replaceAll(
						"(?i)" + Pattern.quote(p.getName()),
						color
								+ p.getName()
								+ (greentext ? "§a"
										: player.CurrentChannel.DisplayName
												.substring(0, 2)));
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
				if (formattedmessage.matches("(?i).*"
						+ Pattern.quote(nwithoutformatting) + ".*")) {
					p = Bukkit.getPlayer(nicknames.get(n));
					if (NotificationSound == null)
						p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f,
								0.5f); // 2015.08.12.
					else
						p.playSound(p.getLocation(), NotificationSound, 1.0f,
								(float) NotificationPitch); // 2015.08.14.
					MaybeOfflinePlayer.AddPlayerIfNeeded(p.getUniqueId()); // 2015.08.17.
				}
				if (p != null) {
					formattedmessage = formattedmessage.replaceAll(
							"(?i)" + Pattern.quote(nwithoutformatting),
							n
									+ (greentext ? "§a"
											: player.CurrentChannel.DisplayName
													.substring(0, 2)));
				}
			}
		}

		/*
		 * event.setFormat(event .getFormat() .replace( "{rptag}",
		 * (player.RPMode ? (ShowRPTag ? "§2[RP]§r" : "") : "§8[OOC]§r"))
		 * .replace("{buttonflair}", flair) .replace( "{isitwilds}",
		 * (event.getPlayer().getWorld().getName() .equalsIgnoreCase("wilds") ?
		 * "[PVP]" : ""))); // 2015.09.04.
		 */

		event.setCancelled(true);
		if (essentials.getUser(event.getPlayer()).isMuted())
			return;

		StringBuilder json = new StringBuilder();
		json.append("[\"\",");
		json.append(String
				.format("{\"text\":\"[%s]%s\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Copy message\",\"color\":\"blue\"}},clickEvent:{\"action\":\"suggest_command\",\"value\":\"%s\"}},",
						player.CurrentChannel.DisplayName,
						(!player.RPMode ? "[OOC]" : ""), suggestmsg));
		json.append("{\"text\":\" <\"},");
		json.append(String.format("{\"text\":\"%s%s\",", event.getPlayer()
				.getDisplayName(), player.GetFormattedFlair()));
		json.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[");
		json.append(String.format("{\"text\":\"%s\n\",", event.getPlayer()
				.getName()));
		json.append(String
				.format("\"color\":\"aqua\"},{\"text\":\"World: %s\n\",\"color\":\"white\"},",
						event.getPlayer().getWorld().getName()));
		json.append(String.format(
				"{\"text\":\"Respect: %s%s%s\",\"color\":\"white\"}]}}},",
				(player.FCount == Integer.MAX_VALUE - 1 ? player.FCount + "+"
						: player.FCount), (player.UserName != null
						&& !player.UserName.isEmpty() ? "\nUserName: "
						+ player.UserName : ""), (player.PlayerName
						.equals("\nAlpha_Bacca44") ? "\nDeaths: " + AlphaDeaths
						: "")));
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

		if (!hadurls) {
			for (String original : list)
				// Hashtags
				formattedmessage = formattedmessage
						.replace(
								"#" + original,
								String.format(
										"\",\"color\":\"%s\"},{\"text\":\"#%s\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://twitter.com/hashtag/%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open on Twitter\",\"color\":\"blue\"}]}}},{\"text\":\"",
										(greentext ? "green"
												: player.CurrentChannel.Color),
										original, original));
		}

		json.append(String.format("{\"text\":\"%s\",\"color\":\"%s\"}]",
				formattedmessage, (greentext ? "green"
						: player.CurrentChannel.Color)));
		// System.out.println(formattedmessage); // TO!DO: TMP
		if (player.CurrentChannel.equals(Channel.TownChat)
				|| player.CurrentChannel.equals(Channel.NationChat))
			// for (Resident resident :
			// PluginMain.Instance.TU.getResidentMap().values()) {
			for (Player p : PluginMain.GetPlayers()) {
				try {
					Resident resident = PluginMain.Instance.TU.getResidentMap()
							.get(p.getName().toLowerCase());
					if (!resident.getName().equals(event.getPlayer().getName())
							&& resident.getModes().contains("spy"))
						Bukkit.getPlayer(resident.getName()).sendMessage(
								String.format("[SPY-%s] - %s: %s",
										player.CurrentChannel.DisplayName,
										event.getPlayer().getDisplayName(),
										event.getMessage()));
				} catch (Exception e) {
					// e.printStackTrace();
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
				// System.out.println("index: " + index);
				Objective obj = PluginMain.SB.getObjective("town");
				// System.out.println("obj: " + obj);
				for (Player p : PluginMain.GetPlayers()) {
					// System.out.println(town.getName()); //Mute fixed,
					// re-enabled /minecraft:me except when muted, admin and mod
					// channel added, links implemented
					try {
						if (PluginMain.Instance.TU.getResidentMap()
								.get(p.getName().toLowerCase()).getTown()
								.getName().equals(town.getName()))
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
								.get(p.getName().toLowerCase()).getTown()
								.getNation().getName().equals(nation.getName()))
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
		} else if (player.CurrentChannel.equals(Channel.AdminChat)) {
			try { // TODO: Put message JSON into it's structure
				if (!event.getPlayer().isOp()) {
					event.getPlayer().sendMessage(
							"§cYou need to be an OP to use this channel.");
					return;
				}
				Objective obj = PluginMain.SB.getObjective("admin");
				for (Player p : PluginMain.GetPlayers()) {
					if (p.isOp())
						obj.getScore(p.getName()).setScore(1);
					else
						obj.getScore(p.getName()).setScore(0);
				}
				PluginMain.Instance
						.getServer()
						.dispatchCommand(
								PluginMain.Console,
								String.format(
										"tellraw @a[score_admin=%d,score_admin_min=%d] %s",
										1, 1, json.toString()));
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
		} else if (player.CurrentChannel.equals(Channel.ModChat)) {
			try {
				if (!PermissionsEx.getUser(event.getPlayer()).inGroup("mod")) {
					event.getPlayer().sendMessage(
							"§cYou need to be a mod to use this channel.");
					return;
				}
				Objective obj = PluginMain.SB.getObjective("mod");
				for (Player p : PluginMain.GetPlayers()) {
					if (PermissionsEx.getUser(p).inGroup("mod"))
						obj.getScore(p.getName()).setScore(1);
					else
						obj.getScore(p.getName()).setScore(0);
				}
				PluginMain.Instance.getServer().dispatchCommand(
						PluginMain.Console,
						String.format(
								"tellraw @a[score_mod=%d,score_mod_min=%d] %s",
								1, 1, json.toString()));
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
		// System.out.println("JSON: " + json); // TO!DO: TMP
		PluginMain.Instance
				.getServer()
				.getConsoleSender()
				.sendMessage(
						String.format("[%s] <%s%s> %s",
								player.CurrentChannel.DisplayName, event
										.getPlayer().getDisplayName(), player
										.GetFormattedFlair(), event
										.getMessage()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void PlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().length() < 2)
			return;
		int index = event.getMessage().indexOf(" ");
		MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(event
				.getPlayer().getUniqueId());
		String cmd = "";
		if (index == -1) {
			cmd = event.getMessage().substring(1);
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
			} else if (cmd.equalsIgnoreCase(Channel.AdminChat.Command)) {
				if (mp.CurrentChannel.equals(Channel.AdminChat))
					mp.CurrentChannel = Channel.GlobalChat;
				else
					mp.CurrentChannel = Channel.AdminChat;
				event.getPlayer().sendMessage(
						"§6You are now talking in: §b"
								+ mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			} else if (cmd.equalsIgnoreCase(Channel.ModChat.Command)) {
				if (mp.CurrentChannel.equals(Channel.ModChat))
					mp.CurrentChannel = Channel.GlobalChat;
				else
					mp.CurrentChannel = Channel.ModChat;
				event.getPlayer().sendMessage(
						"§6You are now talking in: §b"
								+ mp.CurrentChannel.DisplayName);
				event.setCancelled(true);
			}
		} else {
			cmd = event.getMessage().substring(1, index);
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
			} else if (cmd.equalsIgnoreCase(Channel.AdminChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.AdminChat;
				event.getPlayer().chat(event.getMessage().substring(index + 1));
				mp.CurrentChannel = c;
			} else if (cmd.equalsIgnoreCase(Channel.ModChat.Command)) {
				event.setCancelled(true);
				Channel c = mp.CurrentChannel;
				mp.CurrentChannel = Channel.ModChat;
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
			} else if (cmd.equalsIgnoreCase("minecraft:me")) {
				if (!essentials.getUser(event.getPlayer()).isMuted()) {
					event.setCancelled(true);
					String message = event.getMessage().substring(index + 1);
					for (Player p : PluginMain.GetPlayers())
						p.sendMessage(String.format("* %s %s", event
								.getPlayer().getDisplayName(), message));
				}
			}
		}
		if (cmd.equalsIgnoreCase("sethome")) {
			TownyUniverse tu = PluginMain.Instance.TU;
			try {
				TownBlock tb = WorldCoord.parseWorldCoord(event.getPlayer())
						.getTownBlock();
				if (tb.hasTown()) {
					Town town = tb.getTown();
					if (town.hasNation()) {
						Resident res = tu.getResidentMap().get(
								event.getPlayer().getName());
						if (res != null && res.hasTown()) {
							Town town2 = res.getTown();
							if (town2.hasNation()) {
								if (town.getNation().getEnemies()
										.contains(town2.getNation())) {
									event.getPlayer()
											.sendMessage(
													"§cYou cannot set homes in enemy territory.");
									event.setCancelled(true);
									return;
								}
							}
						}
					}
				}
			} catch (NotRegisteredException e) {
				return;
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
	public static int AlphaDeaths;

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity().getName().equals("Alpha_Bacca44"))
			AlphaDeaths++;
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
		e.setCancelled(true); // TODO: /tableflip /unflip with spam detection
		// System.out.println("H");
	}

	@EventHandler
	@SuppressWarnings("deprecation")
	public void onVotifierEvent(VotifierEvent event) {
		Vote vote = event.getVote();
		System.out.println("Vote: " + vote);
		org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(vote
				.getUsername());
		if (op != null) {
			PluginMain.economy.depositPlayer(op, 50.0);
		}

	}
}
