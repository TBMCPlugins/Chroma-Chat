package io.github.norbipeti.thebuttonmc;

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

		if (player != null
				&& PlayerListener.essentials.getUser(player).isMuted())
			return true;

		MaybeOfflinePlayer mp = null;
		if (player != null) {
			mp = MaybeOfflinePlayer.AllPlayers.get(player.getUniqueId());
			if (message.equalsIgnoreCase("F")) {
				if (!mp.PressedF && PlayerListener.ActiveF) {
					PlayerListener.FCount++;
					mp.PressedF = true;
					if (PlayerListener.FPlayer != null
							&& PlayerListener.FPlayer.FCount < Integer.MAX_VALUE - 1)
						PlayerListener.FPlayer.FCount++;
				}
			}
		}

		String msg = message.toLowerCase();
		if (msg.contains("lol")) {
			if (player != null) {
				Commands.Lastlol = player;
				Commands.Lastlolornot = true;
				Commands.Lastlolconsole = false;
			} else {
				Commands.Lastlolornot = true;
				Commands.Lastlolconsole = true;
				Commands.Lastlol = null;
			}
		} else {
			for (int i = 0; i < PlayerListener.LaughStrings.length; i++) {
				if (msg.contains(PlayerListener.LaughStrings[i])) {
					if (player != null) {
						Commands.Lastlol = player;
						Commands.Lastlolornot = false;
						Commands.Lastlolconsole = false;
					} else {
						Commands.Lastlolornot = false;
						Commands.Lastlolconsole = true;
						Commands.Lastlol = null;
					}
					break;
				}
			}
		}
		Channel currentchannel = (mp == null ? PlayerListener.ConsoleChannel
				: mp.CurrentChannel);

		String colormode = currentchannel.Color;
		if (mp != null && mp.OtherColorMode.length() > 0)
			colormode = mp.OtherColorMode;
		if (message.startsWith(">"))
			colormode = "green"; // If greentext, ignore channel or player
									// colors

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
		boolean cont = true;
		while (cont) {

			int first_under = formattedmessage.indexOf("_");
			if (first_under != -1
					&& formattedmessage.indexOf("_", first_under + 1) != -1) // underline
			{
				formattedmessage = formattedmessage.replaceFirst("_", "§n")
						.replaceFirst("_", "§r");
				continue;
			}

			int first_bold = formattedmessage.indexOf("**");
			if (first_bold != -1
					&& formattedmessage.indexOf("**", first_bold + 1) != -1) // bold
			{
				formattedmessage = formattedmessage
						.replaceFirst("\\*\\*", "§l").replaceFirst("\\*\\*",
								"§r");
				continue;
			}
			int first = formattedmessage.indexOf('*');
			if (first != -1 && formattedmessage.indexOf('*', first + 1) != -1) {
				formattedmessage = formattedmessage.replaceFirst("\\*", "§o")
						.replaceFirst("\\*", "§r");
				continue;
			}
			cont = false;
		}

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
										colormode, url, colormode, url));
				hadurls = true;
			} catch (MalformedURLException e) {
			}
			if (mp != null && mp.RainbowPresserColorMode) {
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

		if (!hadurls) {
			for (Player p : PluginMain.GetPlayers()) {
				String color = "";
				if (formattedmessage.matches("(?i).*"
						+ Pattern.quote(p.getName()) + ".*")) {
					if (PlayerListener.NotificationSound == null)
						p.playSound(p.getLocation(),
								Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // TODO:
																			// Airhorn
					else
						p.playSound(p.getLocation(),
								PlayerListener.NotificationSound, 1.0f,
								(float) PlayerListener.NotificationPitch);
					MaybeOfflinePlayer mpp = MaybeOfflinePlayer
							.AddPlayerIfNeeded(p.getUniqueId());
					color = String.format(
							"§%x",
							(mpp.GetFlairColor() == 0x00 ? 0xb : mpp
									.GetFlairColor()));
				}

				formattedmessage = formattedmessage
						.replaceAll(
								"(?i)" + Pattern.quote(p.getName()),
								String.format(
										"\",\"color\":\"%s\"},{\"text\":\"%s%s%s\",\"color\":\"blue\"},{\"text\":\"",
										colormode, color, p.getName(), "§r"));
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
						p.playSound(p.getLocation(),
								Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // 2015.08.12.
					else
						p.playSound(p.getLocation(),
								PlayerListener.NotificationSound, 1.0f,
								(float) PlayerListener.NotificationPitch);
					MaybeOfflinePlayer.AddPlayerIfNeeded(p.getUniqueId());
				}
				if (p != null) {

					formattedmessage = formattedmessage
							.replaceAll(
									"(?i)" + Pattern.quote(nwithoutformatting),
									String.format(
											"\",\"color\":\"%s\"},{\"text\":\"%s%s\",\"color\":\"blue\"},{\"text\":\"",
											colormode, n, "§r"));
				}
			}

			if (formattedmessage.matches("(?i).*" + Pattern.quote("@console")
					+ ".*")) {
				formattedmessage = formattedmessage.replaceAll(
						"(?i)" + Pattern.quote("@console"), "§b@console§r");
				formattedmessage = formattedmessage
						.replaceAll(
								"(?i)" + Pattern.quote("@console"),
								String.format(
										"\",\"color\":\"%s\"},{\"text\":\"§b@console§r\",\"color\":\"blue\"},{\"text\":\"",
										colormode));
				System.out.println("\007");
			}
		}

		StringBuilder json = new StringBuilder();
		json.append("[\"\",");
		json.append(String
				.format("%s{\"text\":\"[%s]%s\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Copy message\",\"color\":\"blue\"}},\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"%s\"}},",
						(mp != null && mp.ChatOnly ? "{\"text\":\"[C]\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Chat only\",\"color\":\"blue\"}]}}},"
								// (mp != null && mp.ChatOnly ?
								// "{\"text:\":\"\"}," - I have been staring at
								// this one line for hours... Hours...
								: ""), currentchannel.DisplayName, (mp != null
								&& !mp.RPMode ? "[OOC]" : ""), suggestmsg));
		json.append("{\"text\":\" <\"},");
		json.append(String.format("{\"text\":\"%s%s\",",
				(player != null ? player.getDisplayName() : sender.getName()),
				(mp != null ? mp.GetFormattedFlair() : "")));
		json.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[");
		json.append(String.format("{\"text\":\"%s\n\",",
				(player != null ? player.getName() : sender.getName())));
		json.append(String
				.format("\"color\":\"aqua\"},{\"text\":\"World: %s\n\",\"color\":\"white\"},",
						(player != null ? player.getWorld().getName() : "-")));
		json.append(String
				.format("{\"text\":\"Respect: %s%s%s\",\"color\":\"white\"}]}}},",
						(mp != null ? (mp.FCount / (double) mp.FDeaths)
								: "Infinite"),
						(mp != null && mp.UserName != null
								&& !mp.UserName.isEmpty() ? "\nUserName: "
								+ mp.UserName : ""),
						(mp != null && mp.PlayerName.equals("\nAlpha_Bacca44") ? "\nDeaths: "
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
										colormode, original, original));
		}

		json.append(String.format("{\"text\":\"%s\",\"color\":\"%s\"}]",
				formattedmessage, colormode));
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
						obj.getScore(p.getName()).setScore(-1);
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
								(mp != null ? mp.GetFormattedFlair() : ""),
								message));
		return true;
	}
}
