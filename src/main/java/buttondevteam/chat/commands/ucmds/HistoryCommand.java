package buttondevteam.chat.commands.ucmds;

import buttondevteam.lib.chat.Channel;
import buttondevteam.lib.chat.ChatMessage;
import buttondevteam.lib.chat.CommandClass;
import lombok.RequiredArgsConstructor;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

@CommandClass
public class HistoryCommand extends UCommandBase {
	private static HashMap<Channel, LinkedList<HistoryEntry>> messages = new HashMap<>();

	@Override
	public String[] GetHelpText(String alias) {
		return new String[]{ //
				"§6--- Chat History ----", //
				"Returns the last 10 messages the player can see." //
		};
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		return showHistory(sender, alias, args, this);
	}

	public static boolean showHistory(CommandSender sender, String alias, String[] args, @Nullable HistoryCommand hc) {
		Function<Map.Entry<Channel, LinkedList<HistoryEntry>>, Map.Entry<Channel, LinkedList<HistoryEntry>>> filterThem = e -> {
			int score = e.getKey().getMCScore(sender);
			LinkedList<HistoryEntry> he = new LinkedList<>();
			for (int i = 0; i < 10 && i < e.getValue().size(); i++) {
				val heh = e.getValue().get(i);
				val cm = heh.chatMessage;
				if (score == e.getKey().getMCScore(cm.getPermCheck()))
					he.push(heh);
			}
			return new HashMap.SimpleEntry<>(e.getKey(), he);
		};
		sender.sendMessage("§6---- Chat History ----");
		Stream<Map.Entry<Channel, LinkedList<HistoryEntry>>> stream;
		if (args.length == 0) {
			stream = messages.entrySet().stream();
		} else {
			Optional<Channel> och = Channel.getChannels().stream().filter(chan -> chan.ID.equalsIgnoreCase(args[0])).findAny();
			if (!och.isPresent()) {
				sender.sendMessage("§cChannel not found. Use the ID, for example: /" + (hc == null ? "u history" : hc.GetCommandPath()) + " ooc");
				return true;
			}
			val hes = messages.get(och.get());
			if (hes == null)
				stream = Stream.empty();
			else
				stream = Stream.of(new HashMap.SimpleEntry<>(och.get(), hes));
		}
		AtomicBoolean sent = new AtomicBoolean();
		val arr = stream.map(filterThem).flatMap(e -> e.getValue().stream())
				.sorted(Comparator.comparingLong(he -> he.timestamp)).toArray(HistoryEntry[]::new);
		for (int i = Math.max(0, arr.length - 10); i < arr.length; i++) {
			HistoryEntry e = arr[i];
			val cm = e.chatMessage;
			sender.sendMessage("[" + e.channel.DisplayName + "] " + cm.getSender().getName() + ": " + cm.getMessage());
			sent.set(true);
		}
		if (!sent.get())
			sender.sendMessage("No messages can be found.");
		return true;
	}

	@RequiredArgsConstructor
	private static class HistoryEntry {
		/**
		 * System.nanoTime()
		 */
		private final long timestamp;
		private final ChatMessage chatMessage;
		private final Channel channel;
	}

	public static void addChatMessage(ChatMessage chatMessage, Channel channel) {
		var ll = messages.computeIfAbsent(channel, k -> new LinkedList<>()); //<-- TIL
		ll.add(new HistoryEntry(System.nanoTime(), chatMessage, channel)); //Adds as last element
		while (ll.size() > 10)
			ll.remove(); //Removes the first element
	}
}
