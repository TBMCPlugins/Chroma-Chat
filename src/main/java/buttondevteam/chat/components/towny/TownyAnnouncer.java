package buttondevteam.chat.components.towny;

import buttondevteam.chat.ChatProcessing;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.chat.TBMCChatAPI;
import com.palmergames.bukkit.towny.TownyLogger;
import lombok.val;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class TownyAnnouncer {
	private static final Pattern LOG_TYPE_PATTERN = Pattern.compile("\\[(\\w+) (?:Msg|Message)](?: (\\w+):)?");
	private static final Handler HANDLER = new Handler() {
		@Override
		public void publish(LogRecord logRecord) {
			if (logRecord.getMessage() == null) return;
			val m = LOG_TYPE_PATTERN.matcher(logRecord.getMessage());
			if (!m.find()) return;
			String groupID = m.group(2); //The group ID is correctly cased
			switch (String.valueOf(m.group(1))) { //valueOf: Handles null
				case "Town":
					if (townChannel == null) return;
					TBMCChatAPI.SendSystemMessage(townChannel,
						new Channel.RecipientTestResult(TownyComponent.getTownNationIndex(groupID, false), groupID),
						logRecord.getMessage(), target, ChatProcessing.MCORIGIN);
					break;
				case "Nation":
					if (nationChannel == null) return;
					TBMCChatAPI.SendSystemMessage(nationChannel,
						new Channel.RecipientTestResult(TownyComponent.getTownNationIndex(groupID, true), groupID),
						logRecord.getMessage(), target, ChatProcessing.MCORIGIN);
					break;
				case "Global":
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat,
						Channel.RecipientTestResult.ALL,
						logRecord.getMessage(), target, ChatProcessing.MCORIGIN);
					break;
			}
		}

		@Override
		public void flush() {

		}

		@Override
		public void close() throws SecurityException {

		}
	};

	private static TBMCSystemChatEvent.BroadcastTarget target;
	private static Channel townChannel;
	private static Channel nationChannel;

	public static void setup(Channel townChannel, Channel nationChannel) {
		target = TBMCSystemChatEvent.BroadcastTarget.add("towny");
		TownyAnnouncer.townChannel = townChannel;
		TownyAnnouncer.nationChannel = nationChannel;
		TownyLogger.log.addHandler(HANDLER);
	}

	public static void setdown() {
		TBMCSystemChatEvent.BroadcastTarget.remove(target);
		target = null;
		TownyAnnouncer.townChannel = null;
		TownyAnnouncer.nationChannel = null;
		TownyLogger.log.removeHandler(HANDLER);
	}
}
