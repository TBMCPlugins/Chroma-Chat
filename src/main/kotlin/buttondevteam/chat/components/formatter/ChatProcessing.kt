package buttondevteam.chat.components.formatter;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.ChatUtils;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.VanillaUtils;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.chat.components.chatonly.ChatOnlyComponent;
import buttondevteam.chat.components.formatter.formatting.*;
import buttondevteam.chat.components.fun.FunComponent;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.chat.listener.PlayerListener;
import buttondevteam.core.ComponentManager;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.ChromaUtils;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.TBMCChatEventBase;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerBase;
import com.earth2me.essentials.User;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.val;
import net.ess3.api.events.AfkStatusChangeEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.suggestCommand;
import static net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT;
import static net.kyori.adventure.text.event.HoverEvent.hoverEvent;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ChatProcessing {
	private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");
	private static final Pattern URL_PATTERN = Pattern.compile("(http[\\w:/?=$\\-_.+!*'(),&]+(?:#[\\w]+)?)");
	private static final Pattern MASKED_LINK_PATTERN = Pattern.compile("\\[([^\\[\\]]+)]\\(([^()]+)\\)");
	private static final NamedTextColor[] RainbowPresserColors = new NamedTextColor[]{RED, GOLD, YELLOW, GREEN,
		BLUE, DARK_PURPLE};
	private static final Pattern WORD_PATTERN = Pattern.compile("\\S+");
	private static final Pattern GREENTEXT_PATTERN = Pattern.compile("^>(?:.|\\s)*");
	private static boolean pingedconsole = false;

	private static final ArrayList<MatchProviderBase> commonFormatters = Lists.newArrayList(
		new RangeMatchProvider("bold", "**", FormatSettings.builder().bold(true).build()),
		new RangeMatchProvider("italic", "*", FormatSettings.builder().italic(true).build()),
		new RangeMatchProvider("underlined", "__", FormatSettings.builder().underlined(true).build()),
		new RangeMatchProvider("italic2", "_", FormatSettings.builder().italic(true).build()),
		new RangeMatchProvider("strikethrough", "~~", FormatSettings.builder().strikethrough(true).build()),
		new RangeMatchProvider("spoiler", "||", FormatSettings.builder().obfuscated(true)
			.onmatch((match, cf, fs) -> {
				cf.setHoverText(match);
				return match;
			}).build()),
		new StringMatchProvider("nullMention", FormatSettings.builder().color(DARK_RED).build(), true, "null"), // Properly added a bug as a feature
		new StringMatchProvider("consolePing", FormatSettings.builder().color(AQUA)
			.onmatch((match, builder, section) -> {
				if (!pingedconsole) {
					System.out.print("\007");
					pingedconsole = true; // Will set it to false in ProcessChat
				}
				return "@console";
			}).build(), true, "@console"),

		new StringMatchProvider("cyan", FormatSettings.builder().color(AQUA).build(), true, "cyan"), // #55
		new RangeMatchProvider("code", "`", FormatSettings.builder().color(DARK_GRAY).build()),
		new RegexMatchProvider("maskedLink", MASKED_LINK_PATTERN, FormatSettings.builder().underlined(true)
			.onmatch((match, builder, section) -> {
				String text, link;
				if (section.Matches.size() < 2 || (text = section.Matches.get(0)).length() == 0 || (link = section.Matches.get(1)).length() == 0)
					return "[MISSING LINK]"; //Doesn't actually happen, because of the regex
				builder.setOpenlink(link);
				return text;
			}).build()),
		new RegexMatchProvider("url", URL_PATTERN, FormatSettings.builder().underlined(true).openlink("$1").build()),
		new RegexMatchProvider("hashtag", HASHTAG_PATTERN, FormatSettings.builder().color(BLUE).openlink("https://twitter.com/hashtag/$1").build()),
		new StringMatchProvider("someone", FormatSettings.builder().color(AQUA)
			.onmatch((match, builder, section) -> {
				if (Bukkit.getOnlinePlayers().size() == 0) return match;
				var players = ImmutableList.copyOf(Bukkit.getOnlinePlayers());
				var playerC = new Random().nextInt(players.size());
				var player = players.get(playerC);
				playPingSound(player, ComponentManager.getIfEnabled(FormatterComponent.class));
				return "@someone (" + player.getDisplayName() + "§r)";
			}).build(), true, "@someone"),
		new RegexMatchProvider("greentext", GREENTEXT_PATTERN, FormatSettings.builder().color(GREEN).build()));
	private static final String[] testPlayers = {"Koiiev", "iie", "Alisolarflare", "NorbiPeti", "Arsen_Derby_FTW", "carrot_lynx"};

	private ChatProcessing() {
	}

	public static boolean ProcessChat(TBMCChatEvent e, FormatterComponent component) {
		Channel channel = e.getChannel();
		ChromaGamerBase cuser = e.getUser();
		String message = e.getMessage();
		long processstart = System.nanoTime();
		Player player = (cuser instanceof TBMCPlayerBase ? ((TBMCPlayerBase) cuser).getPlayer() : null);
		User user = PluginMain.essentials.getUser(player);

		if (player != null && PluginMain.essentials.getSettings().cancelAfkOnInteract()) {
			user.updateActivity(true, AfkStatusChangeEvent.Cause.CHAT); //Could talk in a private channel, so broadcast
			if (user.isMuted())
				return true;
		}

		ChatPlayer mp;
		if (player != null)
			mp = TBMCPlayerBase.getPlayer(player.getUniqueId(), ChatPlayer.class);
		else //Due to the online player map, getPlayer() can be more efficient than getAs()
			mp = e.getUser().getAs(ChatPlayer.class); //May be null

		if (mp != null) {
			if (System.nanoTime() - mp.LastMessageTime < 1000L * 1000L * component.minTimeBetweenMessages.get()) { //0.1s by default
				cuser.sendMessage("§cYou are sending messages too quickly!");
				return true;
			}
			mp.LastMessageTime = System.nanoTime();
		}
		//DimensionManager.a()
		//IRegistry.ae
		//Bukkit.createWorld()
		//MinecraftServer.reload()
		//IRegistry
		//CraftServer

		doFunStuff(cuser, e, message);

		final String channelidentifier = getChannelID(channel, e.getOrigin());

		PluginMain.Instance.getServer().getConsoleSender()
			.sendMessage(String.format("%s <%s§r> %s", channelidentifier, cuser.getName(), message));

		if (Bukkit.getOnlinePlayers().size() == 0) return false; //Don't try to send to nobody (errors on 1.14)

		TextColor colormode = NAMES.value(channel.color.get().getName());
		boolean colorModeChanged = false;
		if (mp != null && mp.OtherColorMode != null) {
			colormode = NAMES.value(mp.OtherColorMode.getName());
			colorModeChanged = true;
		}

		ArrayList<MatchProviderBase> formatters;
		if (component.allowFormatting.get()) {
			formatters = addFormatters(sender -> e.shouldSendTo(ChromaGamerBase.getFromSender(sender)), component);
			if (colorModeChanged && mp.RainbowPresserColorMode) { // Only overwrite channel color
				createRPC(colormode, formatters);
			}
			pingedconsole = false; // Will set it to true onmatch (static constructor)
		} else
			formatters = Lists.newArrayList();

		TextComponent.Builder builder = createEmptyMessageLine(cuser, message, player, channelidentifier, e.getOrigin());
		long combinetime = System.nanoTime();
		ChatFormatter.Combine(formatters, message, builder, component.getConfig(), FormatSettings.builder().color(colormode).build());
		combinetime = System.nanoTime() - combinetime;

		try {
			if (!channel.isGlobal()) {
				String senderGroup = e.getGroupID(cuser);
				if (senderGroup == null) { // Never send messages if the group is null
					cuser.sendMessage("§cYou don't have permission to send this message or something went wrong");
					return true;
				}
				val tc = ComponentManager.getIfEnabled(TownyComponent.class);
				Consumer<Player> spyConsumer = null;
				if (tc != null)
					spyConsumer = tc.handleSpiesInit(channel, builder);
				for (Player p : Bukkit.getOnlinePlayers()) {
					final String group;
					if (player != null
						&& PluginMain.essentials.getUser(p).isIgnoredPlayer(PluginMain.essentials.getUser(player)))
						group = null; // Don't send the message to them
					else
						group = VanillaUtils.getGroupIfChatOn(p, e);
					if (senderGroup.equals(group)) {
						p.sendMessage(builder.build());
						if (tc != null) spyConsumer.accept(p);
					}
					//Only sends if didn't send normally
				}
			} else
				for (Player p : Bukkit.getOnlinePlayers())
					p.sendMessage(builder.build());
		} catch (Exception ex) {
			TBMCCoreAPI.SendException("An error occured while sending a chat message!", ex, PluginMain.Instance);
			cuser.sendMessage("§cAn error occured while sending the message.");
			return true;
		}
		DebugCommand.SendDebugMessage(
			"-- Full ChatProcessing time: " + (System.nanoTime() - processstart) / 1000000f + " ms");
		DebugCommand.SendDebugMessage("-- ChatFormatter.Combine time: " + combinetime / 1000000f + " ms");
		return false;
	}

	static void createRPC(TextColor colormode, ArrayList<MatchProviderBase> formatters) {
		final AtomicInteger rpc = new AtomicInteger(0);
		formatters.add(new RegexMatchProvider("rpc", WORD_PATTERN, FormatSettings.builder().color(colormode).onmatch((match, cf, s) -> {
			cf.setColor(RainbowPresserColors[rpc.getAndUpdate(i -> ++i < RainbowPresserColors.length ? i : 0)]);
			return match;
		}).build()));
	}

	static TextComponent.Builder createEmptyMessageLine(ChromaGamerBase user, String message, @Nullable Player player,
	                                                    final String channelidentifier, String origin) {
		val json = text();
		ChatOnlyComponent.tellrawCreate(user.getAs(ChatPlayer.class), json);
		val channelHover = (ChatUtils.MCORIGIN.equals(origin) ? "" : "From " + origin + "\n") + "Copy message";
		json.append(text(channelidentifier)
			.hoverEvent(hoverEvent(SHOW_TEXT, text(channelHover).color(BLUE))).clickEvent(suggestCommand(message)));
		if (player != null) {
			if (PluginMain.permission.has(player, "tbmc.badge.diamond")) // TODO: Cross-platform permissions
				json.append(text("[P]").color(AQUA).decorate(TextDecoration.BOLD)
					.hoverEvent(hoverEvent(SHOW_TEXT, text("Diamond Patreon supporter"))));
			else if (PluginMain.permission.has(player, "tbmc.badge.gold"))
				json.append(text("[P]").color(GOLD).decorate(TextDecoration.BOLD)
					.hoverEvent(hoverEvent(SHOW_TEXT, text("Gold Patreon supporter"))));
		}
		json.append(text(" <"));
		json.append(text(user.getName()).hoverEvent(hoverEvent(SHOW_TEXT, text(user.getInfo(ChromaGamerBase.InfoTarget.MCHover)))));
		json.append(text("> "));
		return json;
	}

	static String getChannelID(Channel channel, String origin) {
		return ("[" + (ChatUtils.MCORIGIN.equals(origin) ? "" : "§8" + origin.charAt(0) + "§r|") + channel.displayName.get())
			+ "]";
	}

	static ArrayList<MatchProviderBase> addFormatters(Predicate<Player> canSee, @Nullable FormatterComponent component) {
		@SuppressWarnings("unchecked")
		ArrayList<MatchProviderBase> formatters = (ArrayList<MatchProviderBase>) commonFormatters.clone();

		boolean nottest; //Not assigning a default value, so that it can only be used in the if
		if ((nottest = Bukkit.getOnlinePlayers().size() > 0) || ChromaUtils.isTest()) {
			String[] names;
			if (nottest)
				names = Bukkit.getOnlinePlayers().stream().filter(canSee).map(CommandSender::getName).toArray(String[]::new);
			else {
				names = new String[testPlayers.length];
				System.arraycopy(testPlayers, 0, names, 0, testPlayers.length);
			}
			String[] nicknames = Bukkit.getOnlinePlayers().stream().filter(canSee).map(Player::getUniqueId).map(PlayerListener.nicknames.inverse()::get)
				.filter(Objects::nonNull).toArray(String[]::new);

			Consumer<String> error = message -> {
				if (PluginMain.Instance != null)
					PluginMain.Instance.getLogger().warning(message);
				else
					System.out.println(message);
			};

			if (names.length > 0) //Add as first so it handles special characters (_) - though the order of the different types are defined
				formatters.add(0, new StringMatchProvider("name", FormatSettings.builder().color(AQUA)
					.onmatch((match, builder, section) -> {
						Player p = Bukkit.getPlayer(match);
						Optional<String> pn = nottest ? Optional.empty()
							: Arrays.stream(testPlayers).filter(tp -> tp.equalsIgnoreCase(match)).findAny();
						if (nottest ? p == null : pn.isEmpty()) {
							error.accept("Error: Can't find player " + match + " but was reported as online.");
							return "§c" + match + "§r";
						}
						ChatPlayer mpp = TBMCPlayer.getPlayer(nottest ? p.getUniqueId() : new UUID(0, 0), ChatPlayer.class);
						if (nottest) {
							playPingSound(p, component);
						}
						String color = String.format("§%x", (mpp.GetFlairColor() == 0x00 ? 0xb : mpp.GetFlairColor()));
						return color + (nottest ? p.getName() : pn.get()) + "§r"; //Fix name casing, except when testing
					}).build(), true, names));

			if (nicknames.length > 0) //Add as first so it handles special characters
				formatters.add(0, new StringMatchProvider("nickname", FormatSettings.builder().color(AQUA)
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
					}).build(), true, nicknames));
		}
		return formatters;
	}

	private static void playPingSound(Player p, @Nullable FormatterComponent component) {
		if (component == null || component.notificationSound.get().length() == 0)
			p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f); // TODO: Airhorn
		else
			p.playSound(p.getLocation(), component.notificationSound.get(), 1.0f,
				component.notificationPitch.get());
	}

	static void doFunStuff(ChromaGamerBase user, TBMCChatEventBase event, String message) {
		val fc = ComponentManager.getIfEnabled(FunComponent.class);
		if (fc != null) fc.onChat(user, event, message);
	}
}
