package buttondevteam.chat;

import buttondevteam.chat.ObjectTestRunner.Objects;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.chat.formatting.ChatFormatter;
import buttondevteam.chat.formatting.TellrawEvent;
import buttondevteam.chat.formatting.TellrawEvent.ClickAction;
import buttondevteam.chat.formatting.TellrawEvent.HoverAction;
import buttondevteam.chat.formatting.TellrawPart;
import buttondevteam.component.channel.Channel;
import buttondevteam.core.TestPrepare;
import buttondevteam.lib.chat.Color;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(ObjectTestRunner.class)
public class ChatFormatIT {
	@Objects
	public static List<Object> data() {
		TestPrepare.PrepareServer();
		final CommandSender sender = Mockito.mock(CommandSender.class);
		DebugCommand.DebugMode = true;
		PluginMain.permission = Mockito.mock(Permission.class);

		List<Object> list = new ArrayList<Object>();

		list.add(new ChatFormatIT(sender, "*test*", new TellrawPart("test").setItalic(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "**test**", new TellrawPart("test").setBold(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "***test***",
				new TellrawPart("test").setBold(true).setItalic(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "***_test_***",
				new TellrawPart("test").setBold(true).setItalic(true).setUnderlined(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "***_~~test~~_***", new TellrawPart("test").setBold(true).setItalic(true)
				.setUnderlined(true).setStrikethrough(true).setColor(Color.White)));
        list.add(new ChatFormatIT(sender, "¯\\\\\\_(ツ)\\_/¯", new TellrawPart("¯\\_(ツ)_/¯").setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "https://google.hu/",
				new TellrawPart("https://google.hu/").setColor(Color.White).setUnderlined(true)
						.setHoverEvent(TellrawEvent.create(HoverAction.SHOW_TEXT,
								new TellrawPart("Click to open").setColor(Color.Blue)))
						.setClickEvent(TellrawEvent.create(ClickAction.OPEN_URL, "https://google.hu/"))));
        list.add(new ChatFormatIT(sender, "*test", new TellrawPart("*test").setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "**test*", new TellrawPart("**test*").setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "***test", new TellrawPart("***test").setColor(Color.White)));
        list.add(new ChatFormatIT(sender, "Koiiev", new TellrawPart("§bKoiiev§r").setColor(Color.Aqua)));
		list.add(new ChatFormatIT(sender, "norbipeti", new TellrawPart("§bNorbiPeti§r").setColor(Color.Aqua)));
        list.add(new ChatFormatIT(sender, "Arsen_Derby_FTW", new TellrawPart("§bArsen_Derby_FTW§r").setColor(Color.Aqua)));
		list.add(new ChatFormatIT(sender, "carrot_lynx", new TellrawPart("§bcarrot_lynx§r").setColor(Color.Aqua)));
		list.add(new ChatFormatIT(sender, "*carrot_lynx*", new TellrawPart("§bcarrot_lynx§r").setItalic(true).setColor(Color.Aqua)));
		list.add(new ChatFormatIT(sender, "https://norbipeti.github.io/", new TellrawPart("https://norbipeti.github.io/")
				.setColor(Color.White).setUnderlined(true)
				.setHoverEvent(TellrawEvent.create(HoverAction.SHOW_TEXT,
						new TellrawPart("Click to open").setColor(Color.Blue)))
				.setClickEvent(TellrawEvent.create(ClickAction.OPEN_URL, "https://norbipeti.github.io/"))));
		list.add(new ChatFormatIT(sender, "*https://norbipeti.github.io/ heh*", new TellrawPart("https://norbipeti.github.io/").setItalic(true).setUnderlined(true)
				.setHoverEvent(TellrawEvent.create(HoverAction.SHOW_TEXT,
						new TellrawPart("Click to open").setColor(Color.Blue)))
				.setClickEvent(TellrawEvent.create(ClickAction.OPEN_URL, "https://norbipeti.github.io/")), new TellrawPart(" heh").setItalic(true)));
		list.add(new ChatFormatIT(sender, "*test _test_ test*", new TellrawPart("test ").setItalic(true).setColor(Color.White),
				new TellrawPart("test").setItalic(true).setUnderlined(true).setColor(Color.White), new TellrawPart(" test").setItalic(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "https://norbipeti.github.io/test?test&test#test", new TellrawPart("https://norbipeti.github.io/test?test&test#test")
				.setColor(Color.White).setUnderlined(true)
				.setHoverEvent(TellrawEvent.create(HoverAction.SHOW_TEXT,
						new TellrawPart("Click to open").setColor(Color.Blue)))
				.setClickEvent(TellrawEvent.create(ClickAction.OPEN_URL, "https://norbipeti.github.io/test?test&test#test"))));

		return list;
	}

	private final CommandSender sender;
	private final String message;
	private final TellrawPart[] extras;

	public ChatFormatIT(CommandSender sender, String message, TellrawPart... expectedextras) {
		this.sender = sender;
		this.message = message;
		this.extras = expectedextras;
	}

	@Test
	public void testMessage() {
		ArrayList<ChatFormatter> cfs = ChatProcessing.addFormatters(Color.White);
		final String chid = ChatProcessing.getChannelID(Channel.GlobalChat, sender, ChatProcessing.MCORIGIN);
		final TellrawPart tp = ChatProcessing.createTellraw(sender, message, null, null, null, chid, ChatProcessing.MCORIGIN);
		ChatFormatter.Combine(cfs, message, tp);
		System.out.println("Testing: " + message);
		// System.out.println(ChatProcessing.toJson(tp));
		final TellrawPart expectedtp = ChatProcessing.createTellraw(sender, message, null, null, null, chid, ChatProcessing.MCORIGIN);
		// System.out.println("Raw: " + ChatProcessing.toJson(expectedtp));
		for (TellrawPart extra : extras)
			expectedtp.addExtra(extra);
		assertEquals(ChatProcessing.toJson(expectedtp), ChatProcessing.toJson(tp));
	}
}
