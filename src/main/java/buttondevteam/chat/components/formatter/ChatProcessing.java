package buttondevteam.chat.components.formatter;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.ChatUtils;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.VanillaUtils;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.chat.components.chatonly.ChatOnlyComponent;
import buttondevteam.chat.components.formatter.formatting.ChatFormatter;
import buttondevteam.chat.components.formatter.formatting.TellrawEvent;
import buttondevteam.chat.components.formatter.formatting.TellrawPart;
import buttondevteam.chat.components.formatter.formatting.TellrawSerializer;
import buttondevteam.chat.components.fun.FunComponent;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.chat.listener.PlayerListener;
import buttondevteam.core.ComponentManager;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCChatEventBase;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.Priority;
import buttondevteam.lib.chat.TellrawSerializableEnum;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerBase;
import com.earth2me.essentials.User;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ChatProcessing {
	private static final Pattern NULL_MENTION_PATTERN = Pattern.compile("null");
	private static final Pattern CYAN_PATTERN = Pattern.compile("cyan");
	private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\\\");
	private static final Pattern CONSOLE_PING_PATTERN = Pattern.compile("(?i)" + Pattern.quote("@console"));
	private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
	private static final Pattern URL_PATTERN = Pattern.compile("(http[\\w:/?=$\\-_.+!*'(),&]+(?:#[\\w]+)?)");
	public static final Pattern ENTIRE_MESSAGE_PATTERN = Pattern.compile(".+");
	private static final Pattern UNDERLINED_PATTERN = Pattern.compile("__");
	private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*");
	private static final Pattern ITALIC_PATTERN_2 = Pattern.compile("_");
	private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*");
	private static final Pattern CODE_PATTERN = Pattern.compile("`");
	private static final Pattern MASKED_LINK_PATTERN = Pattern.compile("\\[([^\\[\\]]+)]\\(([^()]+)\\)");
	private static final Pattern SOMEONE_PATTERN = Pattern.compile("@someone");
	private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("~~");
	private static final Pattern SPOILER_PATTERN = Pattern.compile("\\|\\|");
	private static final Color[] RainbowPresserColors = new Color[]{Color.Red, Color.Gold, Color.Yellow, Color.Green,
		Color.Blue, Color.DarkPurple};
	private static final Pattern WORD_PATTERN = Pattern.compile("\\S+");
	private static boolean pingedconsole = false;

	public static final ChatFormatter ESCAPE_FORMATTER = ChatFormatter.builder("escape", ESCAPE_PATTERN).build();

	private static ArrayList<ChatFormatter> commonFormatters = Lists.newArrayList(
		ChatFormatter.builder("bold", BOLD_PATTERN).bold(true).removeCharCount((short) 2).type(ChatFormatter.Type.Range)
			.priority(Priority.High).build(),
		ChatFormatter.builder("italic", ITALIC_PATTERN).italic(true).removeCharCount((short) 1).type(ChatFormatter.Type.Range).build(),
		ChatFormatter.builder("italic2", ITALIC_PATTERN_2).italic(true).removeCharCount((short) 1).type(ChatFormatter.Type.Range).build(),
		ChatFormatter.builder("underlined", UNDERLINED_PATTERN).underlined(true).removeCharCount((short) 2).type(ChatFormatter.Type.Range)
			.build(),
		ChatFormatter.builder("strikethrough", STRIKETHROUGH_PATTERN).strikethrough(true).removeCharCount((short) 2).type(ChatFormatter.Type.Range)
			.build(),
		ChatFormatter.builder("spoiler", SPOILER_PATTERN).obfuscated(true).removeCharCount((short) 2).type(ChatFormatter.Type.Range)
			.onmatch((match, cf, fs) -> {
				cf.setHoverText(match);
				return match;
			}).build(),
		ESCAPE_FORMATTER, ChatFormatter.builder("nullMention", NULL_MENTION_PATTERN).color(Color.DarkRed).build(), // Properly added a bug as a feature
		ChatFormatter.builder("consolePing", CONSOLE_PING_PATTERN).color(Color.Aqua).onmatch((match, builder, section) -> {
			if (!pingedconsole) {
				System.out.print("\007");
				pingedconsole = true; // Will set it to false in ProcessChat
			}
			return match;
		}).priority(Priority.High).build(),

		ChatFormatter.builder("hashtag", HASHTAG_PATTERN).color(Color.Blue).openlink("https://twitter.com/hashtag/$1")
			.priority(Priority.High).build(),
		ChatFormatter.builder("cyan", CYAN_PATTERN).color(Color.Aqua).build(), // #55
		ChatFormatter.builder("code", CODE_PATTERN).color(Color.DarkGray).removeCharCount((short) 1).type(ChatFormatter.Type.Range)
			.build(),
		ChatFormatter.builder("maskedLink", MASKED_LINK_PATTERN).underlined(true).onmatch((match, builder, section) -> {
			String text, link;
			if (section.Matches.size() < 2 || (text = section.Matches.get(0)).length() == 0 || (link = section.Matches.get(1)).length() == 0)
				return "";
			builder.setOpenlink(link);
			return text;
		}).type(ChatFormatter.Type.Excluder).build(),
		ChatFormatter.builder("url", URL_PATTERN).underlined(true).openlink("$1").type(ChatFormatter.Type.Excluder).build(),
		ChatFormatter.builder("someone", SOMEONE_PATTERN).color(Color.Aqua).onmatch((match, builder, section) -> {
			if (Bukkit.getOnlinePlayers().size() == 0) return match;
			var players = ImmutableList.copyOf(Bukkit.getOnlinePlayers());
			var playerC = new Random().nextInt(players.size());
			var player = players.get(playerC);
			playPingSound(player, ComponentManager.getIfEnabled(FormatterComponent.class));
			return "@someone (" + player.getDisplayName() + "§r)";
		}).build());
	private static Gson gson = new GsonBuilder()
		.registerTypeHierarchyAdapter(TellrawSerializableEnum.class, new TellrawSerializer.TwEnum())
		.registerTypeHierarchyAdapter(Collection.class, new TellrawSerializer.TwCollection())
		.registerTypeAdapter(Boolean.class, new TellrawSerializer.TwBool())
		.registerTypeAdapter(boolean.class, new TellrawSerializer.TwBool()).disableHtmlEscaping().create();
	private static final String[] testPlayers = {"Koiiev", "iie", "Alisolarflare", "NorbiPeti", "Arsen_Derby_FTW", "carrot_lynx"};

	private ChatProcessing() {
	}

	public static boolean ProcessChat(TBMCChatEvent e, FormatterComponent component) {
		Channel channel = e.getChannel();
		CommandSender sender = e.getSender();
		String message = e.getMessage();
		long processstart = System.nanoTime();
		Player player = (sender instanceof Player ? (Player) sender : null);
		User user = PluginMain.essentials.getUser(player);

		if (player != null) {
			user.updateActivity(true); //Could talk in a private channel, so broadcast
			if (user.isMuted())
				return true;
		}

		doFunStuff(sender, e, message);

		final String channelidentifier = getChannelID(channel, e.getOrigin());
		PluginMain.Instance.getServer().getConsoleSender()
			.sendMessage(String.format("%s <%s§r> %s", channelidentifier, getSenderName(sender, player), message));

		if (Bukkit.getOnlinePlayers().size() == 0) return false; //Don't try to send to nobody (errors on 1.14)

		ChatPlayer mp;
		if (player != null)
			mp = TBMCPlayerBase.getPlayer(player.getUniqueId(), ChatPlayer.class);
		else //Due to the online player map, getPlayer() can be more efficient than getAs()
			mp = e.getUser().getAs(ChatPlayer.class); //May be null

		Color colormode = channel.Color().get();
		if (mp != null && mp.OtherColorMode != null)
			colormode = mp.OtherColorMode;
		if (message.startsWith(">"))
			colormode = Color.Green;
		// If greentext, ignore channel or player colors

		ArrayList<ChatFormatter> formatters;
		if (component.allowFormatting().get()) {
			formatters = addFormatters(colormode, e::shouldSendTo, component);
			if (colormode == channel.Color().get() && mp != null && mp.RainbowPresserColorMode) { // Only overwrite channel color
				createRPC(colormode, formatters);
			}
			pingedconsole = false; // Will set it to true onmatch (static constructor)
		} else
			formatters = Lists.newArrayList(ChatFormatter.builder("entireMessage", ENTIRE_MESSAGE_PATTERN)
				.color(Color.White).priority(Priority.Low).build()); //This formatter is necessary

		TellrawPart json = createTellraw(sender, message, player, mp, e.getUser(), channelidentifier, e.getOrigin());
		long combinetime = System.nanoTime();
		ChatFormatter.Combine(formatters, message, json, component.getConfig());
		combinetime = System.nanoTime() - combinetime;
		String jsonstr = toJson(json);
		if (jsonstr.length() >= 32767) {
			sender.sendMessage(
				"§cError: Message too long. Try shortening it, or remove hashtags and other formatting.");
			return true;
		}
		DebugCommand.SendDebugMessage(jsonstr);

		try {
			if (!channel.isGlobal()) {
				String senderGroup = e.getGroupID(sender);
				if (senderGroup == null) { // Never send messages if the group is null
					sender.sendMessage("§cYou don't have permission to send this message or something went wrong");
					return true;
				}
				val tc = ComponentManager.getIfEnabled(TownyComponent.class);
				if (tc != null) tc.handleSpiesInit(channel, json, ChatProcessing::toJson);
				for (Player p : Bukkit.getOnlinePlayers()) {
					final String group;
					if (player != null
						&& PluginMain.essentials.getUser(p).isIgnoredPlayer(PluginMain.essentials.getUser(player)))
						group = null; // Don't send the message to them
					else
						group = VanillaUtils.getGroupIfChatOn(p, e);
					if (senderGroup.equals(group))
						VanillaUtils.tellRaw(p, jsonstr);
					else if (tc != null) tc.handleSpies(channel, p);
					//Only sends if didn't send normally
				}
			} else
				for (Player p : Bukkit.getOnlinePlayers())
					VanillaUtils.tellRaw(p, jsonstr);
		} catch (Exception ex) {
			TBMCCoreAPI.SendException("An error occured while sending a chat message!", ex);
			sender.sendMessage("§cAn error occured while sending the message.");
			return true;
		}
		DebugCommand.SendDebugMessage(
			"-- Full ChatProcessing time: " + (System.nanoTime() - processstart) / 1000000f + " ms");
		DebugCommand.SendDebugMessage("-- ChatFormatter.Combine time: " + combinetime / 1000000f + " ms");
		return false;
	}

	static void createRPC(Color colormode, ArrayList<ChatFormatter> formatters) {
		final AtomicInteger rpc = new AtomicInteger(0);
		formatters.add(ChatFormatter.builder("rpc", WORD_PATTERN).color(colormode).onmatch((match, cf, s) -> {
			cf.setColor(RainbowPresserColors[rpc.getAndUpdate(i -> ++i < RainbowPresserColors.length ? i : 0)]);
			return match;
		}).build());
	}

	public static String toJson(TellrawPart json) {
		return gson.toJson(json);
	}

	static TellrawPart createTellraw(CommandSender sender, String message, @Nullable Player player,
	                                 @Nullable ChatPlayer mp, @Nullable ChromaGamerBase cg, final String channelidentifier,
	                                 String origin) {
		TellrawPart json = new TellrawPart("");
		ChatOnlyComponent.tellrawCreate(mp, json); //TODO: Make nice API
		json.addExtra(
			new TellrawPart(channelidentifier)
				.setHoverEvent(
					TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT,
						new TellrawPart((ChatUtils.MCORIGIN.equals(origin) ? "" : "From " + origin + "n")
							+ "Copy message").setColor(Color.Blue)))
				.setClickEvent(TellrawEvent.create(TellrawEvent.ClickAction.SUGGEST_COMMAND, message)));
		if (PluginMain.permission.has(sender, "tbmc.badge.diamond"))
			json.addExtra(new TellrawPart("[P]").setColor(Color.Aqua).setBold(true)
				.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT, "Diamond Patreon supporter")));
		else if (PluginMain.permission.has(sender, "tbmc.badge.gold"))
			json.addExtra(new TellrawPart("[P]").setColor(Color.Gold).setBold(true)
				.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT, "Gold Patreon supporter")));
		json.addExtra(new TellrawPart(" <"));
		TellrawPart hovertp = new TellrawPart("");
		if (cg != null)
			hovertp.addExtra(new TellrawPart(cg.getInfo(ChromaGamerBase.InfoTarget.MCHover)));
		json.addExtra(new TellrawPart(getSenderName(sender, player))
			.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT, hovertp)));
		json.addExtra(new TellrawPart("> "));
		return json;
	}

	private static String getSenderName(CommandSender sender, Player player) {
		if (player == null)
			return sender.getName();
		return player.getDisplayName();
	}

	static String getChannelID(Channel channel, String origin) {
		return ("[" + (ChatUtils.MCORIGIN.equals(origin) ? "" : "§8" + origin.substring(0, 1) + "§r|") + channel.DisplayName().get())
			+ "]";
	}

	static ArrayList<ChatFormatter> addFormatters(Color colormode, Predicate<Player> canSee, @Nullable FormatterComponent component) {
		@SuppressWarnings("unchecked")
		ArrayList<ChatFormatter> formatters = (ArrayList<ChatFormatter>) commonFormatters.clone();

		formatters.add(
			ChatFormatter.builder("entireMessage", ENTIRE_MESSAGE_PATTERN).color(colormode).priority(Priority.Low).build());

		boolean nottest; //Not assigning a default value, so that it can only be used in the if
		if ((nottest = Bukkit.getOnlinePlayers().size() > 0) || Bukkit.getVersion().equals("test")) {
			StringBuilder namesb = new StringBuilder("(?i)(");
			boolean addNameFormatter = false; //Needed because some names may be filtered out if they can't see the channel
			if (nottest)
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (canSee.test(p)) {
						namesb.append(p.getName()).append("|");
						addNameFormatter = true;
					}
				}
			else {
				for (String testPlayer : testPlayers)
					namesb.append(testPlayer).append("|");
				addNameFormatter = true;
			}
			namesb.deleteCharAt(namesb.length() - 1);
			namesb.append(")");
			StringBuilder nicksb = new StringBuilder("(?i)(");
			boolean addNickFormatter = false;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (!canSee.test(p)) continue;
				final String nick = PlayerListener.nicknames.inverse().get(p.getUniqueId());
				if (nick != null) {
					nicksb.append(nick).append("|");
					addNickFormatter = true;
				}
			}
			nicksb.deleteCharAt(nicksb.length() - 1);
			nicksb.append(")");

			Consumer<String> error = message -> {
				if (PluginMain.Instance != null)
					PluginMain.Instance.getLogger().warning(message);
				else
					System.out.println(message);
			};

			if (addNameFormatter)
				formatters.add(ChatFormatter.builder("name", Pattern.compile(namesb.toString())).color(Color.Aqua)
					.onmatch((match, builder, section) -> {
						Player p = Bukkit.getPlayer(match);
						Optional<String> pn = nottest ? Optional.empty()
							: Arrays.stream(testPlayers).filter(tp -> tp.equalsIgnoreCase(match)).findAny();
						if (nottest ? p == null : !pn.isPresent()) {
							error.accept("Error: Can't find player " + match + " but was reported as online.");
							return "§c" + match + "§r";
						}
						ChatPlayer mpp = TBMCPlayer.getPlayer(nottest ? p.getUniqueId() : new UUID(0, 0), ChatPlayer.class);
						if (nottest) {
							playPingSound(p, component);
						}
						String color = String.format("§%x", (mpp.GetFlairColor() == 0x00 ? 0xb : mpp.GetFlairColor()));
						return color + (nottest ? p.getName() : pn.get()) + "§r"; //Fix name casing, except when testing
					}).priority(Priority.High).type(ChatFormatter.Type.Excluder).build());

			if (addNickFormatter)
				formatters.add(ChatFormatter.builder("nickname", Pattern.compile(nicksb.toString())).color(Color.Aqua)
					.onmatch((match, builder, section) -> {
						if (PlayerListener.nicknames.containsKey(match.toLowerCase())) { //Made a stream and all that but I can actually store it lowercased
							Player p = Bukkit.getPlayer(PlayerListener.nicknames.get(match.toLowerCase()));
							if (p == null) {
								error.accept("Error: Can't find player nicknamed "
									+ match.toLowerCase() + " but was reported as online.");
								return "§c" + match + "§r";
							}
							playPingSound(p, component);
							return PluginMain.essentials.getUser(p).getNickname();
						}
						error.accept("Player nicknamed " + match.toLowerCase()
							+ " not found in nickname map but was reported as online.");
						return "§c" + match + "§r";
					}).priority(Priority.High).type(ChatFormatter.Type.Excluder).build());
		}
		return formatters;
	}

	private static void playPingSound(Player p, @Nullable FormatterComponent component) {
		if (component == null || component.notificationSound().get().length() == 0)
			p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // TODO: Airhorn
		else
			p.playSound(p.getLocation(), component.notificationSound().get(), 1.0f,
				component.notificationPitch().get());
	}

	static void doFunStuff(CommandSender sender, TBMCChatEventBase event, String message) {
		val fc = ComponentManager.getIfEnabled(FunComponent.class);
		if (fc != null) fc.onChat(sender, event, message);
	}
}
