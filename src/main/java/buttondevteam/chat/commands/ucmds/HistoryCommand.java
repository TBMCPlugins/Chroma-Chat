package buttondevteam.chat.commands.ucmds;

import buttondevteam.chat.PluginMain;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.chat.ChatMessage;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

@CommandClass(helpText = {
	"§6--- Chat History ----", //
	"Returns the last 10 messages the player can see." //
})
public class HistoryCommand extends UCommandBase {
	/**
	 * Key: ChannelID_groupID
	 */
	private static final HashMap<String, LinkedList<HistoryEntry>> messages = new HashMap<>();

	@Command2.Subcommand
	public boolean def(CommandSender sender, @Command2.OptionalArg String channel) {
		return showHistory(sender, channel);
	}

	public static boolean showHistory(CommandSender sender, String channel) {
		if (!PluginMain.Instance.storeChatHistory().get()) {
			sender.sendMessage("§6Chat history is disabled");
			return true;
		}
		Function<Channel, LinkedList<HistoryEntry>> getThem = ch -> messages.get(ch.ID + "_" + ch.getGroupID(sender)); //If can't see, groupID is null, and that shouldn't be in the map
		sender.sendMessage("§6---- Chat History ----");
		Stream<Channel> stream;
		if (channel == null) {
			stream = Channel.getChannels();
		} else {
			Optional<Channel> och = Channel.getChannels().filter(chan -> chan.ID.equalsIgnoreCase(channel)).findAny();
			if (!och.isPresent()) {
				sender.sendMessage("§cChannel not found. Use the ID, for example: /u history g");
				return true;
			}
			stream = Stream.of(och.get());
		}
		AtomicBoolean sent = new AtomicBoolean();
		synchronized (messages) {
			val arr = stream.map(getThem).filter(Objects::nonNull).flatMap(Collection::stream)
				.sorted(Comparator.comparingLong(he -> he.timestamp)).toArray(HistoryEntry[]::new);
			for (int i = Math.max(0, arr.length - 10); i < arr.length; i++) {
				HistoryEntry e = arr[i];
				val cm = e.chatMessage;
				sender.sendMessage("[" + e.channel.DisplayName().get() + "] " + cm.getSender().getName() + ": " + cm.getMessage());
				sent.set(true);
			}
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
		if (!PluginMain.Instance.storeChatHistory().get()) return;
		val groupID = channel.getGroupID(chatMessage.getPermCheck());
		if (groupID == null) return; //Just to be sure
		synchronized (messages) {
			var ll = messages.computeIfAbsent(channel.ID + "_" + groupID, k -> new LinkedList<>()); //<-- TIL
			ll.add(new HistoryEntry(System.nanoTime(), chatMessage, channel)); //Adds as last element
			while (ll.size() > 10)
				ll.remove(); //Removes the first element
		}
	}
}
