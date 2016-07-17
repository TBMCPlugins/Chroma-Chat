package buttondevteam.thebuttonmcchat;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import buttondevteam.thebuttonmcchat.ChatFormatter.Priority;
import buttondevteam.thebuttonmcchat.commands.UnlolCommand;

public class ChatProcessing {
	private static boolean pingedconsole = false;

	// Returns e.setCancelled
	public static boolean ProcessChat(CommandSender sender, String message) {
		if (PlayerListener.essentials == null)
			PlayerListener.essentials = (Essentials) (Bukkit.getPluginManager().getPlugin("Essentials"));
		Player player = (sender instanceof Player ? (Player) sender : null);

		if (player != null && PlayerListener.essentials.getUser(player).isMuted())
			return true;

		ChatPlayer mp = null;
		if (player != null) {
			mp = ChatPlayer.OnlinePlayers.get(player.getUniqueId());
			if (message.equalsIgnoreCase("F")) {
				if (!mp.PressedF && PlayerListener.ActiveF) {
					PlayerListener.FCount++;
					mp.PressedF = true;
					if (PlayerListener.FPlayer != null && PlayerListener.FPlayer.FCount < Integer.MAX_VALUE - 1)
						PlayerListener.FPlayer.FCount++;
				}
			}
		}

		String msg = message.toLowerCase();
		if (msg.contains("lol")) {
			UnlolCommand.Lastlolornot = true;
			UnlolCommand.Lastlol = sender;
		} else {
			for (int i = 0; i < PlayerListener.LaughStrings.length; i++) {
				if (msg.contains(PlayerListener.LaughStrings[i])) {
					UnlolCommand.Lastlol = sender;
					UnlolCommand.Lastlolornot = false;
					break;
				}
			}
		}
		Channel currentchannel = (mp == null ? PlayerListener.ConsoleChannel : mp.CurrentChannel);

		ArrayList<ChatFormatter> formatters = new ArrayList<ChatFormatter>();

		ChatFormatter.Color colormode = currentchannel.Color;
		if (mp != null && mp.OtherColorMode != null)
			colormode = mp.OtherColorMode;
		if (mp != null && mp.RainbowPresserColorMode)
			colormode = ChatFormatter.Color.RPC;
		if (message.startsWith(">"))
			colormode = ChatFormatter.Color.Green;
		// If greentext, ignore channel or player colors

		formatters.add(new ChatFormatter(Pattern.compile(".+"), colormode, "", Priority.Low));

		String formattedmessage = message;
		formattedmessage = formattedmessage.replace("\\", "\\\\");
		formattedmessage = formattedmessage.replace("\"", "\\\"");
		// ^ Tellraw support, needed for both the message and suggestmsg

		String suggestmsg = formattedmessage;

		formatters.add(new ChatFormatter(Pattern.compile("(?<!\\\\)\\*\\*((?:\\\\\\*|[^\\*])+[^\\*\\\\])\\*\\*"),
				ChatFormatter.Format.Bold, "$1"));
		formatters.add(new ChatFormatter(Pattern.compile("(?<!\\\\)\\*((?:\\\\\\*|[^\\*])+[^\\*\\\\])\\*"),
				ChatFormatter.Format.Italic, "$1"));
		formatters.add(new ChatFormatter(Pattern.compile("(?<!\\\\)\\_((?:\\\\\\_|[^\\_])+[^\\_\\\\])\\_"),
				ChatFormatter.Format.Underlined, "$1"));

		// URLs + Rainbow text
		formatters.add(new ChatFormatter(Pattern.compile("(http[\\w:/?=$\\-_.+!*'(),]+)"),
				ChatFormatter.Format.Underlined, "$1"));
		/*
		 * formattedmessage = formattedmessage .replace( item, String.format(
		 * "\",\"color\":\"%s\"},{\"text\":\"%s\",\"color\":\"%s\",\"underlined\":\"true\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open URL\",\"color\":\"blue\"}]}}},{\"text\":\""
		 * , colormode, url, colormode, url));
		 */

		if (PluginMain.GetPlayers().size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("(?i)(");
			for (Player p : PluginMain.GetPlayers())
				sb.append(p.getName()).append("|");
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");

			formatters
					.add(new ChatFormatter(Pattern.compile(sb.toString()), ChatFormatter.Color.Aqua, (String match) -> {
						Player p = Bukkit.getPlayer(match);
						if (p == null) {
							System.out.println("Error: Can't find player " + match + " but it was reported as online.");
							return false;
						}
						ChatPlayer mpp = ChatPlayer.GetFromPlayer(p);
						if (PlayerListener.NotificationSound == null)
							p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // TODO:
																										// Airhorn
						else
							p.playSound(p.getLocation(), PlayerListener.NotificationSound, 1.0f,
									(float) PlayerListener.NotificationPitch);
						String color = String.format("§%x", (mpp.GetFlairColor() == 0x00 ? 0xb : mpp.GetFlairColor()));
						return true; // TODO
					}, Priority.High));

			formatters
					.add(new ChatFormatter(Pattern.compile(sb.toString()), ChatFormatter.Color.Aqua, (String match) -> {
						for (String n : PlayerListener.nicknames.keySet()) {
							String nwithoutformatting = new String(n);
							int index;
							while ((index = nwithoutformatting.indexOf("§k")) != -1)
								nwithoutformatting = nwithoutformatting
										.replace("§k" + nwithoutformatting.charAt(index + 2), ""); // Support
																									// for
																									// one
																									// random
																									// char
							while ((index = nwithoutformatting.indexOf('§')) != -1)
								nwithoutformatting = nwithoutformatting
										.replace("§" + nwithoutformatting.charAt(index + 1), "");
							if (!match.equalsIgnoreCase(nwithoutformatting))
								return false; // TODO
							Player p = Bukkit.getPlayer(PlayerListener.nicknames.get(n));
							if (p == null) {
								System.out.println(
										"Error: Can't find player " + match + " but it was reported as online.");
								return false;
							}
							ChatPlayer mpp = ChatPlayer.GetFromPlayer(p);
							if (PlayerListener.NotificationSound == null)
								p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // TODO:
																											// Airhorn
							else
								p.playSound(p.getLocation(), PlayerListener.NotificationSound, 1.0f,
										(float) PlayerListener.NotificationPitch);
							String color = String.format("§%x",
									(mpp.GetFlairColor() == 0x00 ? 0xb : mpp.GetFlairColor()));
						}
						return true; // TODO
					}, Priority.High));
		}

		pingedconsole = false;
		formatters.add(new ChatFormatter(Pattern.compile("(?i)" + Pattern.quote("@console")), ChatFormatter.Color.Aqua,
				(String match) -> {
					if (!pingedconsole) {
						System.out.print("\007");
						pingedconsole = true;
					}
					return true;
				}, Priority.High));

		formatters.add(new ChatFormatter(Pattern.compile("#(\\w+)"), ChatFormatter.Color.Blue,
				"https://twitter.com/hashtag/$1", Priority.High));

		/*
		 * if (!hadurls) {
		 * 
		 * if (formattedmessage.matches("(?i).*" + Pattern.quote("@console") +
		 * ".*")) { formattedmessage = formattedmessage.replaceAll( "(?i)" +
		 * Pattern.quote("@console"), "§b@console§r"); formattedmessage =
		 * formattedmessage .replaceAll( "(?i)" + Pattern.quote("@console"),
		 * String.format(
		 * "\",\"color\":\"%s\"},{\"text\":\"§b@console§r\",\"color\":\"blue\"},{\"text\":\""
		 * , colormode)); System.out.println("\007"); } }
		 */

		StringBuilder json = new StringBuilder();
		json.append("[\"\",");
		json.append(String.format(
				"%s{\"text\":\"[%s]%s\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Copy message\",\"color\":\"blue\"}},\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"%s\"}},",
				(mp != null && mp.ChatOnly
						? "{\"text\":\"[C]\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Chat only\",\"color\":\"blue\"}]}}},"
						// (mp != null && mp.ChatOnly ?
						// "{\"text:\":\"\"}," - I have been staring at
						// this one line for hours... Hours...
						: ""),
				currentchannel.DisplayName, (mp != null && !mp.RPMode ? "[OOC]" : ""), suggestmsg));
		json.append("{\"text\":\" <\"},");
		json.append(String.format("{\"text\":\"%s%s\",", (player != null ? player.getDisplayName() : sender.getName()),
				(mp != null ? mp.GetFormattedFlair() : "")));
		json.append("\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[");
		json.append(String.format("{\"text\":\"%s\n\",", (player != null ? player.getName() : sender.getName())));
		json.append(String.format("\"color\":\"aqua\"},{\"text\":\"World: %s\n\",\"color\":\"white\"},",
				(player != null ? player.getWorld().getName() : "-")));
		json.append(String.format("{\"text\":\"Respect: %s%s%s\",\"color\":\"white\"}]}}},",
				(mp != null ? (mp.FCount / (double) mp.FDeaths) : "Infinite"),
				(mp != null && mp.UserName != null && !mp.UserName.isEmpty() ? "\nUserName: " + mp.UserName : ""),
				(mp != null && mp.PlayerName.equals("\nAlpha_Bacca44") ? "\nDeaths: " + PlayerListener.AlphaDeaths
						: "")));
		json.append("{\"text\":\"> \",\"color\":\"white\"}");

		/*
		 * int index = -1; ArrayList<String> list = new ArrayList<String>();
		 * while ((index = message.indexOf("#", index + 1)) != -1) { int index2
		 * = message.indexOf(" ", index + 1); if (index2 == -1) index2 =
		 * message.length(); int index3 = message.indexOf("#", index + 1); if
		 * (index3 != -1 && index3 < index2) // A # occurs before a // space
		 * index2 = index3; String original = message.substring(index + 1,
		 * index2); list.add(original); }
		 * 
		 * if (!hadurls) { for (String original : list) // Hashtags
		 * formattedmessage = formattedmessage .replace( "#" + original,
		 * String.format(
		 * "\",\"color\":\"%s\"},{\"text\":\"#%s\",\"color\":\"blue\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://twitter.com/hashtag/%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open on Twitter\",\"color\":\"blue\"}]}}},{\"text\":\""
		 * , colormode, original, original)); }
		 */

		/*
		 * json.append(String.format("{\"text\":\"%s\",\"color\":\"%s\"}]",
		 * ChatFormatter.Combine(formatters, formattedmessage), colormode));
		 */
		json.append(ChatFormatter.Combine(formatters, formattedmessage));
		json.append("]");
		String jsonstr = json.toString();
		if (jsonstr.length() >= 32767) {
			sender.sendMessage(
					"§cError: Message too large. Try shortening it, or remove hashtags and other formatting.");
			return true;
		}
		if (currentchannel.equals(Channel.TownChat) || currentchannel.equals(Channel.NationChat)) {
			if (player == null) {
				sender.sendMessage("§cYou are not a player!");
				return true;
			}
			for (Player p : PluginMain.GetPlayers()) {
				try {
					Resident resident = PluginMain.Instance.TU.getResidentMap().get(p.getName().toLowerCase());
					if (!resident.getName().equals(player.getName()) && resident.getModes().contains("spy"))
						Bukkit.getPlayer(resident.getName()).sendMessage(String.format("[SPY-%s] - %s: %s",
								currentchannel.DisplayName, player.getDisplayName(), message));
				} catch (Exception e) {
				}
			}
		}
		if (currentchannel.equals(Channel.TownChat)) {
			try {
				Town town = null;
				try {
					town = PluginMain.Instance.TU.getResidentMap().get(player.getName().toLowerCase()).getTown();
				} catch (NotRegisteredException e) {
				}
				if (town == null) {
					player.sendMessage("§cYou aren't in a town or an error occured.");
					return true;
				}
				int index = PluginMain.Instance.Towns.indexOf(town);
				if (index < 0) {
					PluginMain.Instance.Towns.add(town);
					index = PluginMain.Instance.Towns.size() - 1;
				}
				Objective obj = PluginMain.SB.getObjective("town");
				for (Player p : PluginMain.GetPlayers()) {
					try {
						if (PluginMain.Instance.TU.getResidentMap().get(p.getName().toLowerCase()).getTown().getName()
								.equals(town.getName()))
							obj.getScore(p.getName()).setScore(index);
						else
							obj.getScore(p.getName()).setScore(-1);
					} catch (Exception e) {
						obj.getScore(p.getName()).setScore(-1);
					}
				}
				PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
						String.format("tellraw @a[score_town=%d,score_town_min=%d] %s", index, index, json.toString()));
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
					town = PluginMain.Instance.TU.getResidentMap().get(player.getName().toLowerCase()).getTown();
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
				int index = PluginMain.Instance.Nations.indexOf(nation);
				if (index < 0) {
					PluginMain.Instance.Nations.add(nation);
					index = PluginMain.Instance.Nations.size() - 1;
				}
				Objective obj = PluginMain.SB.getObjective("nation");
				for (Player p : PluginMain.GetPlayers()) {
					try {
						if (PluginMain.Instance.TU.getResidentMap().get(p.getName().toLowerCase()).getTown().getNation()
								.getName().equals(nation.getName()))
							obj.getScore(p.getName()).setScore(index);
						else
							obj.getScore(p.getName()).setScore(-1);
					} catch (Exception e) {
					}
				}
				PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console, String
						.format("tellraw @a[score_nation=%d,score_nation_min=%d] %s", index, index, json.toString()));
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
				PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
						String.format("tellraw @a[score_admin=%d,score_admin_min=%d] %s", 1, 1, json.toString()));
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
				if (player != null && !PluginMain.permission.playerInGroup(player, "mod")) {
					player.sendMessage("§cYou need to be a mod to use this channel.");
					return true;
				}
				Objective obj = PluginMain.SB.getObjective("mod");
				for (Player p : PluginMain.GetPlayers()) {
					if (PluginMain.permission.playerInGroup(p, "mod"))
						obj.getScore(p.getName()).setScore(1);
					else
						obj.getScore(p.getName()).setScore(0);
				}
				PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
						String.format("tellraw @a[score_mod=%d,score_mod_min=%d] %s", 1, 1, json.toString()));
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
		PluginMain.Instance.getServer().getConsoleSender()
				.sendMessage(String.format("[%s] <%s%s> %s", currentchannel.DisplayName,
						(player != null ? player.getDisplayName() : sender.getName()),
						(mp != null ? mp.GetFormattedFlair() : ""), message));
		return true;
	}
}
