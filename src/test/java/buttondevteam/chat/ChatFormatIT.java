package buttondevteam.chat;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import buttondevteam.chat.ObjectTestRunner.Objects;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.chat.formatting.ChatFormatter;
import buttondevteam.chat.formatting.TellrawEvent;
import buttondevteam.chat.formatting.TellrawEvent.ClickAction;
import buttondevteam.chat.formatting.TellrawEvent.HoverAction;
import buttondevteam.chat.formatting.TellrawPart;
import buttondevteam.core.TestPrepare;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.Color;

@RunWith(ObjectTestRunner.class)
public class ChatFormatIT {
	@Objects
	public static List<Object> data() {
		TestPrepare.PrepareServer();
		final CommandSender sender = Mockito.mock(CommandSender.class);
		DebugCommand.DebugMode = true;

		List<Object> list = new ArrayList<Object>();

		list.add(new ChatFormatIT(sender, "*test*", new TellrawPart("test").setItalic(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "**test**", new TellrawPart("test").setBold(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "***test***",
				new TellrawPart("test").setBold(true).setItalic(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "***_test_***",
				new TellrawPart("test").setBold(true).setItalic(true).setUnderlined(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "***_~~test~~_***", new TellrawPart("test").setBold(true).setItalic(true)
				.setUnderlined(true).setStrikethrough(true).setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "¯\\\\\\_(ツ)\\_/¯", new TellrawPart("¯").setColor(Color.White),
				new TellrawPart("\\").setColor(Color.White), new TellrawPart("_(ツ)").setColor(Color.White),
				new TellrawPart("_/¯").setColor(Color.White)));
		list.add(new ChatFormatIT(sender, "https://google.hu/",
				new TellrawPart("https://google.hu/").setColor(Color.White).setUnderlined(true)
						.setHoverEvent(TellrawEvent.create(HoverAction.SHOW_TEXT,
								new TellrawPart("Click to open").setColor(Color.Blue)))
						.setClickEvent(TellrawEvent.create(ClickAction.OPEN_URL, "https://google.hu/"))));

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
		final String chid = ChatProcessing.getChannelID(Channel.GlobalChat, sender);
		final TellrawPart tp = ChatProcessing.createTellraw(sender, message, null, null, chid);
		ChatFormatter.Combine(cfs, message, tp);
		System.out.println("Testing: " + message);
		// System.out.println(ChatProcessing.toJson(tp));
		final TellrawPart expectedtp = ChatProcessing.createTellraw(sender, message, null, null, chid);
		// System.out.println("Raw: " + ChatProcessing.toJson(expectedtp));
		for (TellrawPart extra : extras)
			expectedtp.addExtra(extra);
		assertEquals(ChatProcessing.toJson(expectedtp), ChatProcessing.toJson(tp));
	}
}
