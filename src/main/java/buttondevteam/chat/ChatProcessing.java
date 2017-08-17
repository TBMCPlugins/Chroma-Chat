package buttondevteam.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import com.earth2me.essentials.Essentials;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import buttondevteam.chat.commands.UnlolCommand;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.chat.formatting.*;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.TellrawSerializableEnum;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerBase;
import buttondevteam.chat.listener.PlayerListener;
import buttondevteam.lib.chat.*;

public class ChatProcessing {
	private static final Pattern NULL_MENTION_PATTERN = Pattern.compile("null");
	private static final Pattern CYAN_PATTERN = Pattern.compile("cyan");
	private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\\\");
	private static final Pattern CONSOLE_PING_PATTERN = Pattern.compile("(?i)" + Pattern.quote("@console"));
	private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
	private static final Pattern URL_PATTERN = Pattern.compile("(http[\\w:/?=$\\-_.+!*'(),]+)");
	private static final Pattern ENTIRE_MESSAGE_PATTERN = Pattern.compile(".+");
	private static final Pattern UNDERLINED_PATTERN = Pattern.compile("\\_");
	private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*");
	private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*");
	private static final Color[] RainbowPresserColors = new Color[] { Color.Red, Color.Gold, Color.Yellow, Color.Green,
			Color.Blue, Color.DarkPurple };
	private static boolean pingedconsole = false;

	public static final ChatFormatter ESCAPE_FORMATTER = new ChatFormatterBuilder().setRegex(ESCAPE_PATTERN).build();

	private static ArrayList<ChatFormatter> commonFormatters = Lists.newArrayList(
			new ChatFormatterBuilder().setRegex(BOLD_PATTERN).setBold(true).setRemoveCharCount((short) 2).setRange(true)
					.setPriority(Priority.High).build(),
			new ChatFormatterBuilder().setRegex(ITALIC_PATTERN).setItalic(true).setRemoveCharCount((short) 1)
					.setRange(true).build(),
			new ChatFormatterBuilder().setRegex(UNDERLINED_PATTERN).setUnderlined(true).setRemoveCharCount((short) 1)
					.setRange(true).build(),
			ESCAPE_FORMATTER,
			new ChatFormatterBuilder().setRegex(URL_PATTERN).setUnderlined(true).setOpenlink("$1").build(),
			new ChatFormatterBuilder().setRegex(NULL_MENTION_PATTERN).setColor(Color.DarkRed).build(), // Properly added a bug as a feature
			new ChatFormatterBuilder().setRegex(CONSOLE_PING_PATTERN).setColor(Color.Aqua)
					.setOnmatch((match, builder) ->

					{
						if (!pingedconsole) {
							System.out.print("\007");
							pingedconsole = true; // Will set it to false in ProcessChat
						}
						return match;
					}).setPriority(Priority.High).build(),

			new ChatFormatterBuilder().setRegex(HASHTAG_PATTERN).setColor(Color.Blue)
					.setOpenlink("https://twitter.com/hashtag/$1").setPriority(Priority.High).build(),
			new ChatFormatterBuilder().setRegex(CYAN_PATTERN).setColor(Color.Aqua).build() // #55
	);
	private static Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(TellrawSerializableEnum.class, new TellrawSerializer.TwEnum())
			.registerTypeHierarchyAdapter(Collection.class, new TellrawSerializer.TwCollection())
			.registerTypeAdapter(Boolean.class, new TellrawSerializer.TwBool())
			.registerTypeAdapter(boolean.class, new TellrawSerializer.TwBool()).disableHtmlEscaping().create();

	private ChatProcessing() {
	}

	public static boolean ProcessChat(TBMCChatEvent e) {
		Channel channel = e.getChannel();
		CommandSender sender = e.getSender();
		String message = e.getMessage();
		long processstart = System.nanoTime();
		if (PluginMain.essentials == null)
			PluginMain.essentials = (Essentials) (Bukkit.getPluginManager().getPlugin("Essentials"));
		Player player = (sender instanceof Player ? (Player) sender : null);

		if (player != null && PluginMain.essentials.getUser(player).isMuted())
			return true;

		doFunStuff(sender, message);

		ChatPlayer mp = null;
		if (player != null)
			mp = TBMCPlayerBase.getPlayer(player.getUniqueId(), ChatPlayer.class);

		Color colormode = channel.color;
		if (mp != null && mp.OtherColorMode != null)
			colormode = mp.OtherColorMode;
		if (message.startsWith(">"))
			colormode = Color.Green;
		// If greentext, ignore channel or player colors

		ArrayList<ChatFormatter> formatters = addFormatters(colormode);
		if (colormode == channel.color && mp != null && mp.RainbowPresserColorMode) { // Only overwrite channel color
			final AtomicInteger rpc = new AtomicInteger(0);
			formatters.add(new ChatFormatterBuilder().setColor(colormode).setOnmatch((match, builder) -> {
				builder.setColor(
						RainbowPresserColors[rpc.getAndUpdate(i -> ++i < RainbowPresserColors.length ? i : 0)]);
				return match;
			}).build());
		}
		pingedconsole = false; // Will set it to true onmatch (static constructor)
		final String channelidentifier = getChannelID(channel, sender, mp);

		TellrawPart json = createTellraw(sender, message, player, mp, channelidentifier);
		long combinetime = System.nanoTime();
		ChatFormatter.Combine(formatters, message, json);
		combinetime = System.nanoTime() - combinetime;
		String jsonstr = toJson(json);
		if (jsonstr.length() >= 32767) {
			sender.sendMessage(
					"§cError: Message too long. Try shortening it, or remove hashtags and other formatting.");
			return true;
		}
		DebugCommand.SendDebugMessage(jsonstr);

		try {
			if (channel.filteranderrormsg != null) {
				Objective obj = PluginMain.SB.getObjective(channel.ID);
				int score = -1;
				for (Player p : Bukkit.getOnlinePlayers()) {
					final int mcScore = VanillaUtils.getMCScoreIfChatOn(p, e);
					obj.getScore(p.getName())
							.setScore(p.getUniqueId().equals(player.getUniqueId()) ? score = mcScore : mcScore);
				}
				PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console, String.format(
						"tellraw @a[score_%s=%d,score_%s_min=%d] %s", channel.ID, score, channel.ID, score, jsonstr));
			} else
				PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
						String.format("tellraw @a %s", jsonstr));
		} catch (Exception ex) {
			TBMCCoreAPI.SendException("An error occured while sending a chat message!", ex);
			sender.sendMessage("§cAn error occured while sending the message.");
			return true;
		}
		PluginMain.Instance.getServer().getConsoleSender().sendMessage(String.format("%s <%s> %s", channelidentifier,
				(player != null ? player.getDisplayName() : sender.getName()), message));
		DebugCommand.SendDebugMessage(
				"-- Full ChatProcessing time: " + (System.nanoTime() - processstart) / 1000000f + " ms");
		DebugCommand.SendDebugMessage("-- ChatFormatter.Combine time: " + combinetime / 1000000f + " ms");
		return false;
	}

	static String toJson(TellrawPart json) {
		String jsonstr = gson.toJson(json);
		return jsonstr;
	}

	static TellrawPart createTellraw(CommandSender sender, String message, Player player, ChatPlayer mp,
			final String channelidentifier) {
		TellrawPart json = new TellrawPart("");
		if (mp != null && mp.ChatOnly) {
			json.addExtra(new TellrawPart("[C]")
					.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT, "Chat only")));
		}
		json.addExtra(
				new TellrawPart(channelidentifier)
						.setHoverEvent(
								TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT,
										new TellrawPart((sender instanceof IDiscordSender ? "From Discord\n" : "")
												+ "Copy message").setColor(Color.Blue)))
						.setClickEvent(TellrawEvent.create(TellrawEvent.ClickAction.SUGGEST_COMMAND, message)));
		json.addExtra(new TellrawPart(" <"));
		json.addExtra(
				new TellrawPart(
						(player != null ? player.getDisplayName()
								: sender.getName()))
										.setHoverEvent(
												TellrawEvent
														.create(TellrawEvent.HoverAction.SHOW_TEXT,
																new TellrawPart("")
																		.addExtra(new TellrawPart(String.format(
																				"Flair: %s",
																				(mp != null ? mp.GetFormattedFlair()
																						: "-"))))
																		.addExtra(new TellrawPart(String.format(
																				"\nPlayername: %s\n",
																				(player != null ? player.getName()
																						: sender.getName())))
																								.setColor(Color.Aqua))
																		.addExtra(new TellrawPart(String.format(
																				"World: %s\n",
																				(player != null
																						? player.getWorld().getName()
																						: "-"))))
																		.addExtra(new TellrawPart(String.format(
																				"Respect: %s%s%s",
																				(mp != null ? (mp.FCount().get()
																						/ (double) mp.FDeaths().get())
																						: "Infinite"),
																				(mp != null
																						&& mp.UserName().get() != null
																						&& !mp.UserName().get()
																								.isEmpty()
																										? "\nUserName: "
																												+ mp.UserName()
																														.get()
																										: ""),
																				(mp != null && mp.PlayerName().get()
																						.equals("\nAlpha_Bacca44")
																								? "\nDeaths: "
																										+ PlayerListener.AlphaDeaths
																								: ""))))
																		.addExtra(new TellrawPart(
																				"\nFor more, do /u info "
																						+ sender.getName())))));
		json.addExtra(new TellrawPart("> "));
		return json;
	}

	static String getChannelID(Channel channel, CommandSender sender, ChatPlayer mp) {
		final String channelidentifier = ("[" + (sender instanceof IDiscordSender ? "d|" : "") + channel.DisplayName)
				+ "]" + (mp != null && !mp.RPMode ? "[OOC]" : "");
		return channelidentifier;
	}

	static ArrayList<ChatFormatter> addFormatters(Color colormode) {
		@SuppressWarnings("unchecked")
		ArrayList<ChatFormatter> formatters = (ArrayList<ChatFormatter>) commonFormatters.clone();

		formatters.add(new ChatFormatterBuilder().setRegex(ENTIRE_MESSAGE_PATTERN).setColor(colormode)
				.setPriority(Priority.Low).build());

		if (Bukkit.getOnlinePlayers().size() > 0) {
			StringBuilder namesb = new StringBuilder("(?i)(");
			for (Player p : Bukkit.getOnlinePlayers())
				namesb.append(p.getName()).append("|");
			namesb.deleteCharAt(namesb.length() - 1);
			namesb.append(")");
			StringBuilder nicksb = new StringBuilder("(?i)(");
			boolean addNickFormatter = false;
			final int size = Bukkit.getOnlinePlayers().size();
			int index = 0;
			for (Player p : Bukkit.getOnlinePlayers()) {
				final String nick = PlayerListener.nicknames.inverse().get(p.getUniqueId());
				if (nick != null) {
					nicksb.append(nick);
					if (index < size - 1) {
						nicksb.append("|");
						addNickFormatter = true;
					}
				}
				index++;
			}
			nicksb.append(")");

			formatters.add(new ChatFormatterBuilder().setRegex(Pattern.compile(namesb.toString())).setColor(Color.Aqua)
					.setOnmatch((match, builder) -> {
						Player p = Bukkit.getPlayer(match);
						if (p == null) {
							PluginMain.Instance.getLogger()
									.warning("Error: Can't find player " + match + " but was reported as online.");
							return "§c" + match + "§r";
						}
						ChatPlayer mpp = TBMCPlayer.getPlayer(p.getUniqueId(), ChatPlayer.class);
						if (PlayerListener.NotificationSound == null)
							p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // TODO: Airhorn
						else
							p.playSound(p.getLocation(), PlayerListener.NotificationSound, 1.0f,
									(float) PlayerListener.NotificationPitch);
						String color = String.format("§%x", (mpp.GetFlairColor() == 0x00 ? 0xb : mpp.GetFlairColor()));
						return color + p.getName() + "§r";
					}).setPriority(Priority.High).build());

			if (addNickFormatter)
				formatters.add(new ChatFormatterBuilder().setRegex(Pattern.compile(nicksb.toString()))
						.setColor(Color.Aqua).setOnmatch((match, builder) -> {
							if (PlayerListener.nicknames.containsKey(match)) {
								Player p = Bukkit.getPlayer(PlayerListener.nicknames.get(match));
								if (p == null) {
									PluginMain.Instance.getLogger().warning("Error: Can't find player nicknamed "
											+ match + " but was reported as online.");
									return "§c" + match + "§r";
								}
								if (PlayerListener.NotificationSound == null)
									p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f);
								else
									p.playSound(p.getLocation(), PlayerListener.NotificationSound, 1.0f,
											(float) PlayerListener.NotificationPitch);
								return PluginMain.essentials.getUser(p).getNickname();
							}
							Bukkit.getServer().getLogger().warning("Player nicknamed " + match
									+ " not found in nickname map but was reported as online.");
							return "§c" + match + "§r";
						}).setPriority(Priority.High).build());
		}
		return formatters;
	}

	static void doFunStuff(CommandSender sender, String message) {
		if (PlayerListener.ActiveF && !PlayerListener.Fs.contains(sender) && message.equalsIgnoreCase("F"))
			PlayerListener.Fs.add(sender);

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
	}
}
