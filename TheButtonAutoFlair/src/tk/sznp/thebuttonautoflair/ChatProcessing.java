package tk.sznp.thebuttonautoflair;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

public class ChatProcessing {
	// Returns e.setCancelled
	public static boolean ProcessChat(CommandSender sender, String message) {
		if (PlayerListener.essentials == null)
			PlayerListener.essentials = (Essentials) (Bukkit.getPluginManager()
					.getPlugin("Essentials"));
		Player player = (sender instanceof Player ? (Player) sender : null);
		if (player != null && message.equalsIgnoreCase("F")) {
			MaybeOfflinePlayer mp = MaybeOfflinePlayer.AllPlayers.get(player
					.getUniqueId());
			if (!mp.PressedF && PlayerListener.ActiveF) {
				PlayerListener.FCount++;
				mp.PressedF = true;
				if (PlayerListener.FPlayer != null
						&& PlayerListener.FPlayer.FCount < Integer.MAX_VALUE - 1)
					PlayerListener.FPlayer.FCount++;
			}
		}

		boolean greentext = message.startsWith(">");
		String msg = message.toLowerCase();
		if (player != null && msg.contains("lol")) {
			Commands.Lastlol = MaybeOfflinePlayer.AllPlayers.get(player
					.getUniqueId());
			Commands.Lastlolornot = true;
		} else {
			for (int i = 0; i < PlayerListener.LaughStrings.length; i++) {
				if (msg.contains(PlayerListener.LaughStrings[i])) {
					Commands.Lastlol = MaybeOfflinePlayer.AllPlayers.get(player
							.getUniqueId());
					Commands.Lastlolornot = false;
					break;
				}
			}
		}

		MaybeOfflinePlayer mplayer = null;
		if (player != null) {
			mplayer = MaybeOfflinePlayer.AllPlayers.get(player.getUniqueId());
		}
		Channel currentchannel = (mplayer == null ? PlayerListener.ConsoleChannel
				: mplayer.CurrentChannel);
		String formattedmessage = message;
		formattedmessage = formattedmessage.replace("\\", "\\\\"); // It's
																	// really
																	// important
																	// to escape
																	// the
																	// slashes
																	// first
		formattedmessage = formattedmessage.replace("\"", "\\\"");
		if (PluginMain.permission.has(sender, "tbmc.admin"))
			formattedmessage = formattedmessage.replace("&", "§");
		formattedmessage = formattedmessage.replace("§r", "§"
				+ currentchannel.DisplayName.charAt(1));
		String suggestmsg = formattedmessage;

		// URLs + Rainbow text
		String[] parts = formattedmessage.split("\\s+");
		boolean hadurls = false;
		final String[] RainbowPresserColors = new String[] { "c", "6", "e",
				"a", "9", "5" };
		int rpc = 0;
		int currentindex = 0;
		for (String item : parts) {
			try {
				URL url = new URL(item);
				formattedmessage = formattedmessage
						.replace(
								item,
								String.format(
										"\",\"color\":\"%s\"},{\"text\":\"%s\",\"color\":\"%s\",\"underlined\":\"true\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open URL\",\"color\":\"blue\"}]}}},{\"text\":\"",
										(greentext ? "green"
												: currentchannel.Color), url,
										(greentext ? "green"
												: currentchannel.Color), url));
				hadurls = true;
			} catch (MalformedURLException e) {
			}
			if (mplayer != null && mplayer.RainbowPresserColorMode) { // TODO:
																		// Rainbow
																		// mode
																		// for
				// console
				if (item.startsWith(RainbowPresserColors[rpc])) { // Prevent
																	// words
																	// being
																	// equal/starting
																	// with a
																	// color
																	// code
																	// letter to
																	// be messed
																	// up
					if (rpc + 1 < RainbowPresserColors.length)
						rpc++;
					else
						rpc = 0;
				}
				StringBuffer buf = new StringBuffer(formattedmessage);
				buf.replace(currentindex, currentindex + item.length(),
						String.format("§%s%s", RainbowPresserColors[rpc], item));
				formattedmessage = buf.toString();
				if (rpc + 1 < RainbowPresserColors.length)
					rpc++;
				else
					rpc = 0;
			}
			currentindex += item.length() + 3;
		}
		if (mplayer != null && mplayer.OtherColorMode != 0xFF) {
			formattedmessage = String.format("§%x%s", mplayer.OtherColorMode,
					formattedmessage);
		}

		if (!hadurls) {
			for (Player p : PluginMain.GetPlayers()) {
				String color = "";
				if (formattedmessage.matches("(?i).*"
						+ Pattern.quote(p.getName()) + ".*")) {
					if (PlayerListener.NotificationSound == null)
						p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f,
								0.5f);
					else
						p.playSound(p.getLocation(),
								PlayerListener.NotificationSound, 1.0f,
								(float) PlayerListener.NotificationPitch);
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
										: currentchannel.DisplayName.substring(
												0, 2)));
			}
			for (String n : PlayerListener.nicknames.keySet()) {
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
					p = Bukkit.getPlayer(PlayerListener.nicknames.get(n));
					if (PlayerListener.NotificationSound == null)
						p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f,
								0.5f); // 2015.08.12.
					else
						p.playSound(p.getLocation(),
								PlayerListener.NotificationSound, 1.0f,
								(float) PlayerListener.NotificationPitch);
					MaybeOfflinePlayer.AddPlayerIfNeeded(p.getUniqueId());
				}
				if (p != null) {
					formattedmessage = formattedmessage.replaceAll(
							"(?i)" + Pattern.quote(nwithoutformatting),
							n
									+ (greentext ? "§a"
											: currentchannel.DisplayName
													.substring(0, 2)));
				}
			}

			if (formattedmessage.matches("(?i).*" + Pattern.quote("@console")
					+ ".*")) {
				formattedmessage = formattedmessage.replaceAll(
						"(?i)" + Pattern.quote("@console"), "§b@console§r");
				System.out.println("\007");
			}
		}

		if (player != null
				&& PlayerListener.essentials.getUser(player).isMuted())
			return true;

		StringBuilder json = new StringBuilder();
		json.append("[\"\",");
		json.append(String
				.format("{\"text\":\"[%s]%s\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Copy message\",\"color\":\"blue\"}},\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"%s\"}},",
						currentchannel.DisplayName, (mplayer != null
								&& !mplayer.RPMode ? "[OOC]" : ""), suggestmsg));
		json.append("{\"text\":\" <\"},");
		json.append(String.format("{\"text\":\"%s%s\",",
				(player != null ? player.getDisplayName() : sender.getName()),
				(mplayer != null ? mplayer.GetFormattedFlair() : "")));
		json.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[");
		json.append(String.format("{\"text\":\"%s\n\",",
				(player != null ? player.getName() : sender.getName())));
		json.append(String
				.format("\"color\":\"aqua\"},{\"text\":\"World: %s\n\",\"color\":\"white\"},",
						(player != null ? player.getWorld().getName() : "-")));
		json.append(String.format(
				"{\"text\":\"Respect: %s%s%s\",\"color\":\"white\"}]}}},",
				(mplayer != null ? (mplayer.FCount / (double) mplayer.FDeaths)
						: "Infinite"),
				(mplayer != null && mplayer.UserName != null
						&& !mplayer.UserName.isEmpty() ? "\nUserName: "
						+ mplayer.UserName : ""),
				(mplayer != null
						&& mplayer.PlayerName.equals("\nAlpha_Bacca44") ? "\nDeaths: "
						+ PlayerListener.AlphaDeaths
						: "")));
		json.append("{\"text\":\"> \",\"color\":\"white\"},");

		int index = -1;
		ArrayList<String> list = new ArrayList<String>();
		while ((index = message.indexOf("#", index + 1)) != -1) {
			int index2 = message.indexOf(" ", index + 1);
			if (index2 == -1)
				index2 = message.length();
			int index3 = message.indexOf("#", index + 1);
			if (index3 != -1 && index3 < index2) // A # occurs before a
													// space
				index2 = index3;
			String original = message.substring(index + 1, index2);
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
												: currentchannel.Color),
										original, original));
		}

		json.append(String.format("{\"text\":\"%s\",\"color\":\"%s\"}]",
				formattedmessage, (greentext ? "green" : currentchannel.Color)));
		String jsonstr = json.toString();
		if (jsonstr.length() >= 32767) {
			sender.sendMessage("§cError: Message too large. Try shortening it, or remove hashtags and other formatting.");
			return true;
		}
		if (currentchannel.equals(Channel.TownChat)
				|| currentchannel.equals(Channel.NationChat)) {
			if (player == null) {
				sender.sendMessage("§cYou are not a player!");
				return true;
			}
			for (Player p : PluginMain.GetPlayers()) {
				try {
					Resident resident = PluginMain.Instance.TU.getResidentMap()
							.get(p.getName().toLowerCase());
					if (!resident.getName().equals(player.getName())
							&& resident.getModes().contains("spy"))
						Bukkit.getPlayer(resident.getName()).sendMessage(
								String.format("[SPY-%s] - %s: %s",
										currentchannel.DisplayName,
										player.getDisplayName(), message));
				} catch (Exception e) {
				}
			}
		}
		if (currentchannel.equals(Channel.TownChat)) {
			try {
				Town town = null;
				try {
					town = PluginMain.Instance.TU.getResidentMap()
							.get(player.getName().toLowerCase()).getTown();
				} catch (NotRegisteredException e) {
				}
				if (town == null) {
					player.sendMessage("§cYou aren't in a town or an error occured.");
					return true;
				}
				index = PluginMain.Instance.Towns.indexOf(town);
				if (index < 0) {
					PluginMain.Instance.Towns.add(town);
					index = PluginMain.Instance.Towns.size() - 1;
				}
				Objective obj = PluginMain.SB.getObjective("town");
				for (Player p : PluginMain.GetPlayers()) {
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
				player.sendMessage("§cAn error occured while sending the message. (IllegalStateException)");
				return true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				player.sendMessage("§cAn error occured while sending the message. (IllegalArgumentException)");
				return true;
			}
		} else if (currentchannel.equals(Channel.NationChat)) {
			try {
				Town town = null;
				try {
					town = PluginMain.Instance.TU.getResidentMap()
							.get(player.getName().toLowerCase()).getTown();
				} catch (NotRegisteredException e) {
				}
				if (town == null) {
					player.sendMessage("§cYou aren't in a town or an error occured.");
					return true;
				}
				Nation nation = null;
				try {
					nation = town.getNation();
				} catch (NotRegisteredException e) {
				}
				if (nation == null) {
					player.sendMessage("§cYour town isn't in a nation or an error occured.");
					return true;
				}
				index = PluginMain.Instance.Nations.indexOf(nation);
				if (index < 0) {
					PluginMain.Instance.Nations.add(nation);
					index = PluginMain.Instance.Nations.size() - 1;
				}
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
				player.sendMessage("§cAn error occured while sending the message. (IllegalStateException)");
				return true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				player.sendMessage("§cAn error occured while sending the message. (IllegalArgumentException)");
				return true;
			}
		} else if (currentchannel.equals(Channel.AdminChat)) {
			try { // TODO: Put message JSON into it's structure
				if (player != null && !player.isOp()) {
					player.sendMessage("§cYou need to be an OP to use this channel.");
					return true;
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
				player.sendMessage("§cAn error occured while sending the message. (IllegalStateException)");
				return true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				player.sendMessage("§cAn error occured while sending the message. (IllegalArgumentException)");
				return true;
			}
		} else if (currentchannel.equals(Channel.ModChat)) {
			try {
				if (player != null
						&& !PermissionsEx.getUser(player).inGroup("mod")) {
					player.sendMessage("§cYou need to be a mod to use this channel.");
					return true;
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
				player.sendMessage("§cAn error occured while sending the message. (IllegalStateException)");
				return true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				player.sendMessage("§cAn error occured while sending the message. (IllegalArgumentException)");
				return true;
			}
		} else
			PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
					String.format("tellraw @a %s", json.toString()));
		PluginMain.Instance
				.getServer()
				.getConsoleSender()
				.sendMessage(
						String.format("[%s] <%s%s> %s",
								currentchannel.DisplayName,
								(player != null ? player.getDisplayName()
										: sender.getName()),
								(mplayer != null ? mplayer.GetFormattedFlair()
										: ""), message));
		System.out.println(json.toString()); //TODO: TMP
		System.out.println("Col 107: "+json.toString().charAt(107)); //TODO: TMP
		return true;
	}
}
