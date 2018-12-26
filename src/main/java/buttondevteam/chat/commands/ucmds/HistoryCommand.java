package buttondevteam.chat.commands.ucmds;

import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.ChatMessage;
import buttondevteam.lib.chat.CommandClass;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

@CommandClass
public class HistoryCommand extends UCommandBase {
	private static HashMap<Channel, HistoryEntry[]> messages = new HashMap<>();

	@Override
	public String[] GetHelpText(String alias) {
		return new String[]{ //
				"§6--- Chat History ----", //
				"Returns the last 10 messages the player can see." //
		};
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		Function<Map.Entry<Channel, HistoryEntry[]>, Map.Entry<Channel, HistoryEntry[]>> filterThem = e -> {
			int score = e.getKey().getMCScore(sender);
			HistoryEntry[] he = new HistoryEntry[10];
			for (int i = 0, j = 0; i < 10; i++) {
				val cm = e.getValue()[i].chatMessage;
				if (cm == null)
					break; //Don't have 10 messages yet
				if (score == e.getKey().getMCScore(cm.getPermCheck()))
					he[j++] = e.getValue()[i];
			}
			return new HashMap.SimpleEntry<>(e.getKey(), he);
		};
		sender.sendMessage("§6---- Chat History ----");
		Stream<HistoryEntry> stream;
		if (args.length == 0) {
			stream = messages.entrySet().stream().map(filterThem).flatMap(e -> Arrays.stream(e.getValue()));
		} else {
			Channel ch = Channel.GlobalChat; //TODO: Channel param
			val hes = messages.get(ch);
			if (hes == null)
				return true;
			stream = Arrays.stream(hes);
		}
		AtomicBoolean sent = new AtomicBoolean();
		stream.sorted(Comparator.comparingLong(he -> he.timestamp)).forEach(e -> {
			val cm = e.chatMessage;
			sender.sendMessage(cm.getSender().getName() + ": " + cm.getMessage());
			sent.set(true);
		});
		if (!sent.get())
			sender.sendMessage("No messages can be found.");
		return true;
	}

	@RequiredArgsConstructor
	public static class HistoryEntry {
		/**
		 * System.nanoTime()
		 */
		private final long timestamp;
		private final ChatMessage chatMessage;
	}
}
