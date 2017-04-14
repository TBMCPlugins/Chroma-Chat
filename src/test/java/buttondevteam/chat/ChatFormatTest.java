package buttondevteam.chat;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.junit.Test;
import org.mockito.Mockito;

import buttondevteam.chat.formatting.ChatFormatter;
import buttondevteam.chat.formatting.TellrawPart;
import buttondevteam.core.TestPrepare;
import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.Format;
import junit.framework.TestCase;

public class ChatFormatTest extends TestCase {
	@Test
	public void test() {
		// fail("Not yet implemented");
		TestPrepare.PrepareServer();
		final CommandSender sender = Mockito.mock(CommandSender.class);
		testMessage(sender, "*test*", new TellrawPart("test").setFormat(Format.Italic.getFlag()).setColor(Color.White));
		testMessage(sender, "**test**", new TellrawPart("test").setFormat(Format.Bold.getFlag()).setColor(Color.White));
		testMessage(sender, "***test***", new TellrawPart("test")
				.setFormat(Format.Bold.getFlag() | Format.Italic.getFlag()).setColor(Color.White));
		testMessage(sender, "***_test_***",
				new TellrawPart("test")
						.setFormat(Format.Bold.getFlag() | Format.Italic.getFlag() | Format.Underlined.getFlag())
						.setColor(Color.White));
		testMessage(sender, "***_~~test~~_***",
				new TellrawPart("test").setFormat(Format.Bold.getFlag() | Format.Italic.getFlag()
						| Format.Underlined.getFlag() | Format.Strikethrough.getFlag()).setColor(Color.White));
	}

	void testMessage(final CommandSender sender, final String message, TellrawPart... extras) {
		ArrayList<ChatFormatter> cfs = ChatProcessing.addFormatters(Color.White);
		final String chid = ChatProcessing.getChannelID(Channel.GlobalChat, sender, null);
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
