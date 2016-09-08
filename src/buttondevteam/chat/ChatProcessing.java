package buttondevteam.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import com.earth2me.essentials.Essentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import buttondevteam.chat.commands.UnlolCommand;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.chat.formatting.ChatFormatter;
import buttondevteam.chat.formatting.ChatFormatterBuilder;
import buttondevteam.chat.formatting.TellrawEvent;
import buttondevteam.chat.formatting.TellrawPart;
import buttondevteam.chat.formatting.TellrawSerializableEnum;
import buttondevteam.chat.formatting.TellrawSerializer;
import buttondevteam.chat.formatting.ChatFormatter.Color;
import buttondevteam.chat.formatting.ChatFormatter.Format;
import buttondevteam.chat.formatting.ChatFormatter.Priority;
import buttondevteam.chat.formatting.TellrawEvent.ClickAction;
import buttondevteam.chat.formatting.TellrawEvent.HoverAction;
import buttondevteam.chat.formatting.TellrawSerializer.TwCollection;
import buttondevteam.chat.formatting.TellrawSerializer.TwEnum;

public class ChatProcessing {
	private static final Pattern CONSOLE_PING_PATTERN = Pattern.compile("(?i)" + Pattern.quote("@console"));
	private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
	private static final Pattern URL_PATTERN = Pattern.compile("(http[\\w:/?=$\\-_.+!*'(),]+)");
	private static final Pattern ENTIRE_MESSAGE_PATTERN = Pattern.compile(".+");
	private static final Pattern UNDERLINED_PATTERN = Pattern.compile("(?<!\\\\)\\_((?:\\\\\\_|[^\\_])+[^\\_\\\\])\\_");
	private static final Pattern ITALIC_PATTERN = Pattern
			.compile("(?<![\\\\\\*])\\*((?:\\\\\\*|[^\\*])+[^\\*\\\\])\\*(?!\\*)");
	private static final Pattern BOLD_PATTERN = Pattern.compile("(?<!\\\\)\\*\\*((?:\\\\\\*|[^\\*])+[^\\*\\\\])\\*\\*");
	private static final String[] RainbowPresserColors = new String[] { "red", "gold", "yellow", "green", "blue",
			"dark_purple" }; // TODO
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

		formatters.add(new ChatFormatterBuilder().setRegex(ENTIRE_MESSAGE_PATTERN).setColor(colormode)
				.setPriority(Priority.Low).build());

		String formattedmessage = message;

		String suggestmsg = formattedmessage;

		formatters.add(new ChatFormatterBuilder().setRegex(BOLD_PATTERN).setFormat(ChatFormatter.Format.Bold)
				.setReplacewith("$1").build());
		formatters.add(new ChatFormatterBuilder().setRegex(ITALIC_PATTERN).setFormat(ChatFormatter.Format.Italic)
				.setReplacewith("$1").build());
		formatters.add(new ChatFormatterBuilder().setRegex(UNDERLINED_PATTERN)
				.setFormat(ChatFormatter.Format.Underlined).setReplacewith("$1").build());
		formatters.add(new ChatFormatterBuilder().setRegex(Pattern.compile("\\\\([\\*\\_\\\\])")).setReplacewith("$1")
				.build());

		// URLs + Rainbow text
		formatters.add(new ChatFormatterBuilder().setRegex(URL_PATTERN).setFormat(ChatFormatter.Format.Underlined)
				.setReplacewith("$1").build());
		/*
		 * formattedmessage = formattedmessage .replace( item, String.format(
		 * "\",\"color\":\"%s\"},{\"text\":\"%s\",\"color\":\"%s\",\"underlined\":\"true\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Open URL\",\"color\":\"blue\"}]}}},{\"text\":\""
		 * , colormode, url, colormode, url));
		 */

		if (PluginMain.GetPlayers().size() > 0) {
			StringBuilder namesb = new StringBuilder();
			namesb.append("(?i)(");
			for (Player p : PluginMain.GetPlayers())
				namesb.append(p.getName()).append("|");
			namesb.deleteCharAt(namesb.length() - 1);
			namesb.append(")");
			StringBuilder nicksb = new StringBuilder();
			nicksb.append("(?i)(");
			for (Player p : PluginMain.GetPlayers())
				nicksb.append(PlayerListener.nicknames.inverse().get(p.getUniqueId())).append("|");
			nicksb.deleteCharAt(nicksb.length() - 1);
			nicksb.append(")");

			formatters.add(new ChatFormatterBuilder().setRegex(Pattern.compile(namesb.toString()))
					.setColor(ChatFormatter.Color.Aqua).setOnmatch((String match) -> {
						Player p = Bukkit.getPlayer(match);
						if (p == null) {
							PluginMain.Instance.getLogger()
									.warning("Error: Can't find player " + match + " but was reported as online.");
							return "§c" + match + "§r";
						}
						ChatPlayer mpp = ChatPlayer.GetFromPlayer(p);
						if (PlayerListener.NotificationSound == null)
							p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // TODO:
																										// Airhorn
						else
							p.playSound(p.getLocation(), PlayerListener.NotificationSound, 1.0f,
									(float) PlayerListener.NotificationPitch);
						String color = String.format("§%x", (mpp.GetFlairColor() == 0x00 ? 0xb : mpp.GetFlairColor()));
						return color + p.getName() + "§r";
					}).setPriority(Priority.High).build());

			formatters.add(new ChatFormatterBuilder().setRegex(Pattern.compile(nicksb.toString()))
					.setColor(ChatFormatter.Color.Aqua).setOnmatch((String match) -> {
						if (PlayerListener.nicknames.containsKey(match)) {
							Player p = Bukkit.getPlayer(PlayerListener.nicknames.get(match));
							if (p == null) {
								PluginMain.Instance.getLogger().warning(
										"Error: Can't find player nicknamed " + match + " but was reported as online.");
								return "§c" + match + "§r";
							}
							if (PlayerListener.NotificationSound == null)
								p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // TODO:
																											// Airhorn
							else
								p.playSound(p.getLocation(), PlayerListener.NotificationSound, 1.0f,
										(float) PlayerListener.NotificationPitch);
							return PlayerListener.essentials.getUser(p).getNickname();
						}
						Bukkit.getServer().getLogger().warning(
								"Player nicknamed " + match + " not found in nickname map but was reported as online.");
						return "§c" + match + "§r";
					}).setPriority(Priority.High).build());
		}

		pingedconsole = false;
		formatters.add(new ChatFormatterBuilder().setRegex(CONSOLE_PING_PATTERN).setColor(ChatFormatter.Color.Aqua)
				.setOnmatch((String match) -> {
					if (!pingedconsole) {
						System.out.print("\007");
						pingedconsole = true;
					}
					return match;
				}).setPriority(Priority.High).build());

		formatters.add(new ChatFormatterBuilder().setRegex(HASHTAG_PATTERN).setColor(ChatFormatter.Color.Blue)
				.setOpenlink("https://twitter.com/hashtag/$1").setPriority(Priority.High).build());

		/*
		 * if (!hadurls) { if (formattedmessage.matches("(?i).*" + Pattern.quote("@console") + ".*")) { formattedmessage = formattedmessage.replaceAll( "(?i)" + Pattern.quote("@console"),
		 * "§b@console§r"); formattedmessage = formattedmessage .replaceAll( "(?i)" + Pattern.quote("@console"), String.format(
		 * "\",\"color\":\"%s\"},{\"text\":\"§b@console§r\",\"color\":\"blue\"},{\"text\":\"" , colormode)); System.out.println("\007"); } }
		 */

		TellrawPart json = new TellrawPart(""); // TODO: Put flair into hovertext
		if (mp != null && mp.ChatOnly) {
			json.addExtra(new TellrawPart("[C]").setHoverEvent(
					TellrawEvent.create(TellrawEvent.HoverAC, TellrawEvent.HoverAction.SHOW_TEXT, "Chat only")));
		}
		json.addExtra(
				new TellrawPart(("[" + currentchannel.DisplayName) + "]" + (mp != null && !mp.RPMode ? "[OOC]" : ""))
						.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAC, TellrawEvent.HoverAction.SHOW_TEXT,
								new TellrawPart("Copy message").setColor(Color.Blue)))
						.setClickEvent(TellrawEvent.create(TellrawEvent.ClickAC,
								TellrawEvent.ClickAction.SUGGEST_COMMAND, suggestmsg)));
		json.addExtra(new TellrawPart(" <"));
		json.addExtra(
				new TellrawPart(
						(player != null ? player.getDisplayName() : sender.getName()))
								.setHoverEvent(
										TellrawEvent
												.create(TellrawEvent.HoverAC, TellrawEvent.HoverAction.SHOW_TEXT,
														new TellrawPart("")
																.addExtra(new TellrawPart(String.format("Flair: %s",
																		(mp != null ? mp.GetFormattedFlair() : "-"))))
																.addExtra(new TellrawPart(
																		String.format("\nPlayername: %s\n",
																				(player != null ? player.getName()
																						: sender.getName())))
																								.setColor(Color.Aqua))
																.addExtra(new TellrawPart(String.format("World: %s\n",
																		(player != null ? player.getWorld().getName()
																				: "-"))))
																.addExtra(new TellrawPart(String.format(
																		"Respect: %s%s%s",
																		(mp != null ? (mp.FCount / (double) mp.FDeaths)
																				: "Infinite"),
																		(mp != null && mp.UserName != null
																				&& !mp.UserName.isEmpty()
																						? "\nUserName: " + mp.UserName
																						: ""),
																		(mp != null && mp.PlayerName.equals(
																				"\nAlpha_Bacca44") ? "\nDeaths: "
																						+ PlayerListener.AlphaDeaths
																						: "")))))));
		json.addExtra(new TellrawPart("> "));
		ChatFormatter.Combine(formatters, formattedmessage, json);
		Gson gson = new GsonBuilder()
				.registerTypeHierarchyAdapter(TellrawSerializableEnum.class, new TellrawSerializer.TwEnum())
				.registerTypeHierarchyAdapter(Collection.class, new TellrawSerializer.TwCollection())
				.registerTypeAdapter(Boolean.class, new TellrawSerializer.TwBool())
				.registerTypeAdapter(boolean.class, new TellrawSerializer.TwBool()).disableHtmlEscaping().create();
		String jsonstr = gson.toJson(json);
		if (jsonstr.length() >= 32767) {
			sender.sendMessage(
					"§cError: Message too long. Try shortening it, or remove hashtags and other formatting.");
			return true;
		}
		DebugCommand.SendDebugMessage(jsonstr);
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
						String.format("tellraw @a[score_town=%d,score_town_min=%d] %s", index, index, jsonstr));
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
				PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
						String.format("tellraw @a[score_nation=%d,score_nation_min=%d] %s", index, index, jsonstr));
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
						String.format("tellraw @a[score_admin=%d,score_admin_min=%d] %s", 1, 1, jsonstr));
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
						String.format("tellraw @a[score_mod=%d,score_mod_min=%d] %s", 1, 1, jsonstr));
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
					String.format("tellraw @a %s", jsonstr));
		PluginMain.Instance.getServer().getConsoleSender()
				.sendMessage(String.format("[%s] <%s%s> %s", currentchannel.DisplayName,
						(player != null ? player.getDisplayName() : sender.getName()),
						(mp != null ? mp.GetFormattedFlair() : ""), message));
		return true;
	}
}
