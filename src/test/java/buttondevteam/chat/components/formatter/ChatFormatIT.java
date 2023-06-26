package buttondevteam.chat.components.formatter;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.ChatUtils;
import buttondevteam.chat.ObjectTestRunner;
import buttondevteam.chat.ObjectTestRunner.Objects;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.chat.components.formatter.formatting.ChatFormatter;
import buttondevteam.chat.components.formatter.formatting.FormatSettings;
import buttondevteam.chat.components.formatter.formatting.MatchProviderBase;
import buttondevteam.core.MainPlugin;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.test.TestPermissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static be.seeseemelk.mockbukkit.MockBukkit.load;
import static be.seeseemelk.mockbukkit.MockBukkit.mock;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.Action.OPEN_URL;
import static net.kyori.adventure.text.event.ClickEvent.clickEvent;
import static net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT;
import static net.kyori.adventure.text.event.HoverEvent.hoverEvent;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

@RunWith(ObjectTestRunner.class)
public class ChatFormatIT {
	@Objects
	public static List<Object> data() {
		mock();
		load(MainPlugin.class, true);
		var sender = ChromaGamerBase.getUser(UUID.randomUUID().toString(), ChatPlayer.class);
		DebugCommand.DebugMode = true;
		PluginMain.permission = new TestPermissions();

		List<Object> list = new ArrayList<>();

		list.add(new ChatFormatIT(sender, "*test*", text("test").decorate(ITALIC).color(WHITE)));
		list.add(new ChatFormatIT(sender, "**test**", text("test").decorate(BOLD).color(WHITE)));
		list.add(new ChatFormatIT(sender, "***test***", text("test").decorate(BOLD, ITALIC).color(WHITE)));
		list.add(new ChatFormatIT(sender, "***__test__***", text("test").decorate(BOLD, ITALIC, UNDERLINED).color(WHITE)));
		list.add(new ChatFormatIT(sender, "***__~~test~~__***", text("test").decorate(BOLD, ITALIC, UNDERLINED, STRIKETHROUGH).color(WHITE)));
		list.add(new ChatFormatIT(sender, "¯\\\\\\_(ツ)\\_/¯", text("¯\\_(ツ)_/¯").color(WHITE)));
		list.add(new ChatFormatIT(sender, "https://google.hu/",
			text("https://google.hu/").color(WHITE).decorate(UNDERLINED)
				.hoverEvent(hoverEvent(SHOW_TEXT, text("Click to open").color(BLUE)))
				.clickEvent(clickEvent(OPEN_URL, "https://google.hu/"))));
		list.add(new ChatFormatIT(sender, "*test", text("*test").color(WHITE)));
		list.add(new ChatFormatIT(sender, "**test*", text("**test*").color(WHITE)));
		list.add(new ChatFormatIT(sender, "***test", text("***test").color(WHITE)));
		list.add(new ChatFormatIT(sender, "Koiiev", text("§bKoiiev§r").color(AQUA)));
		list.add(new ChatFormatIT(sender, "norbipeti", text("§bNorbiPeti§r").color(AQUA)));
		list.add(new ChatFormatIT(sender, "Arsen_Derby_FTW", text("§bArsen_Derby_FTW§r").color(AQUA)));
		list.add(new ChatFormatIT(sender, "carrot_lynx", text("§bcarrot_lynx§r").color(AQUA)));
		list.add(new ChatFormatIT(sender, "*carrot_lynx*", text("§bcarrot_lynx§r").decorate(ITALIC).color(AQUA)));
		list.add(new ChatFormatIT(sender, "https://norbipeti.github.io/", text("https://norbipeti.github.io/")
			.color(WHITE).decorate(UNDERLINED)
			.hoverEvent(hoverEvent(SHOW_TEXT, text("Click to open").color(BLUE)))
			.clickEvent(clickEvent(OPEN_URL, "https://norbipeti.github.io/"))));
		list.add(new ChatFormatIT(sender, "*https://norbipeti.github.io/ heh*", text("https://norbipeti.github.io/").decorate(ITALIC).decorate(UNDERLINED)
			.hoverEvent(hoverEvent(SHOW_TEXT, text("Click to open").color(BLUE)))
			.clickEvent(clickEvent(OPEN_URL, "https://norbipeti.github.io/")).color(WHITE), text(" heh").decorate(ITALIC).color(WHITE)));
		list.add(new ChatFormatIT(sender, "*test _test_ test*", text("test test test").decorate(ITALIC).color(WHITE)));
		list.add(new ChatFormatIT(sender, "*test __test__ test*", text("test ").decorate(ITALIC).color(WHITE),
			text("test").decorate(ITALIC).decorate(UNDERLINED).color(WHITE), text(" test").decorate(ITALIC).color(WHITE)));
		list.add(new ChatFormatIT(sender, "**test __test__ test**", text("test ").decorate(BOLD).color(WHITE),
			text("test").decorate(BOLD).decorate(UNDERLINED).color(WHITE), text(" test").decorate(BOLD).color(WHITE)));
		list.add(new ChatFormatIT(sender, "**test _test_ test**", text("test ").decorate(BOLD).color(WHITE),
			text("test").decorate(ITALIC).decorate(BOLD).color(WHITE), text(" test").decorate(BOLD).color(WHITE)));
		list.add(new ChatFormatIT(sender, "https://norbipeti.github.io/test?test&test#test", text("https://norbipeti.github.io/test?test&test#test")
			.color(WHITE).decorate(UNDERLINED)
			.hoverEvent(hoverEvent(SHOW_TEXT, text("Click to open").color(BLUE)))
			.clickEvent(clickEvent(OPEN_URL, "https://norbipeti.github.io/test?test&test#test"))));
		list.add(new ChatFormatIT(sender, "[hmm](https://norbipeti.github.io/test)", text("hmm")
			.color(WHITE).decorate(UNDERLINED)
			.hoverEvent(hoverEvent(SHOW_TEXT, text("Click to open").color(BLUE)))
			.clickEvent(clickEvent(OPEN_URL, "https://norbipeti.github.io/test"))));
		var space = text(" ").color(WHITE);
		list.add(new ChatFormatIT(sender, "A rainbow text for testing. O", text("A").color(RED),
			space, text("rainbow").color(GOLD), space, text("text").color(YELLOW),
			space, text("for").color(GREEN), space, text("testing.").color(BLUE),
			space, text("O").color(DARK_PURPLE)).setRainbowMode());
		list.add(new ChatFormatIT(sender, "***test*** test", text("test").color(WHITE)
			.decorate(ITALIC).decorate(BOLD), text(" test").color(WHITE)));
		list.add(new ChatFormatIT(sender, ">test message\nheh", text(">test message\nheh").color(GREEN)));
		list.add(new ChatFormatIT(sender, "[here's a link]()", text("[here's a link]()").color(WHITE)));
		list.add(new ChatFormatIT(sender, "[](fakelink)", text("[](fakelink)").color(WHITE)));
		list.add(new ChatFormatIT(sender, "||this is a spoiler||", text("this is a spoiler").color(WHITE)
			.decorate(OBFUSCATED).hoverEvent(hoverEvent(SHOW_TEXT, text("this is a spoiler").color(WHITE)))));
		Function<String, TextComponent> whiteBoldItalic = text -> text(text).color(WHITE).decorate(BOLD).decorate(ITALIC);
		list.add(new ChatFormatIT(sender, "***some complicated ||test message|| with [links](https://chromagaming.figytuna.com) and other __greatness__ by NorbiPeti***",
			whiteBoldItalic.apply("some complicated "),
			whiteBoldItalic.apply("test message").decorate(OBFUSCATED).hoverEvent(hoverEvent(SHOW_TEXT, text("test message"))),
			whiteBoldItalic.apply(" with "),
			whiteBoldItalic.apply("links").clickEvent(clickEvent(OPEN_URL, "https://chromagaming.figytuna.com"))
				.decorate(UNDERLINED).hoverEvent(hoverEvent(SHOW_TEXT, text("Click to open").color(BLUE))),
			whiteBoldItalic.apply(" and other "),
			whiteBoldItalic.apply("greatness").decorate(UNDERLINED),
			whiteBoldItalic.apply(" by "),
			whiteBoldItalic.apply("§bNorbiPeti§r").color(AQUA))); //§b: flair color
		list.add(new ChatFormatIT(sender, "hey @console", text("hey ").color(WHITE),
			text("@console").color(AQUA)));

		return list;
	}

	private final ChromaGamerBase sender;
	private final String message;
	private final Component[] extras;
	private boolean rainbowMode;

	public ChatFormatIT(ChromaGamerBase sender, String message, Component... expectedExtras) {
		this.sender = sender;
		this.message = message;
		this.extras = expectedExtras;
	}

	private ChatFormatIT setRainbowMode() {
		rainbowMode = true;
		return this;
	}

	@Test
	public void testMessage() {
		System.out.println("Testing: " + message);
		ArrayList<MatchProviderBase> cfs = ChatProcessing.addFormatters(p -> true, null);
		final String chid = ChatProcessing.getChannelID(Channel.globalChat, ChatUtils.MCORIGIN);
		if (rainbowMode)
			ChatProcessing.createRPC(WHITE, cfs);
		final TextComponent.Builder tp = ChatProcessing.createEmptyMessageLine(sender, message, null, chid, ChatUtils.MCORIGIN);
		ChatFormatter.Combine(cfs, message, tp, null, FormatSettings.builder().color(WHITE).build());
		final TextComponent.Builder expectedtp = ChatProcessing.createEmptyMessageLine(sender, message, null, chid, ChatUtils.MCORIGIN);
		for (Component extra : extras)
			expectedtp.append(extra);
		Assert.assertEquals(expectedtp.build(), tp.build());
	}
}
