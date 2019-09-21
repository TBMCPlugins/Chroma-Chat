package buttondevteam.chat.components.towny;

import buttondevteam.chat.ChatProcessing;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.chat.TBMCChatAPI;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.regex.Pattern;

public class TownyAnnouncer {
	private static final Pattern LOG_TYPE_PATTERN = Pattern.compile("\\[(\\w+) (?:Msg|Message)](?: (\\w+):)?");
	private static final Appender HANDLER = new AbstractAppender(TownyAnnouncer.class.getSimpleName(),
		LevelRangeFilter.createFilter(Level.INFO, Level.INFO, Filter.Result.ACCEPT, Filter.Result.DENY),
		PatternLayout.createDefaultLayout()) {
		@Override
		public void append(LogEvent logRecord) {
			if (logRecord.getMessage() == null) return;
			String message = logRecord.getMessage().toString();
			val m = LOG_TYPE_PATTERN.matcher(message);
			if (!m.find()) return;
			String groupID = m.group(2); //The group ID is correctly cased
			switch (String.valueOf(m.group(1))) { //valueOf: Handles null
				case "Town":
					if (townChannel == null) return;
					TBMCChatAPI.SendSystemMessage(townChannel,
						new Channel.RecipientTestResult(TownyComponent.getTownNationIndex(groupID, false), groupID),
						message, target, ChatProcessing.MCORIGIN);
					break;
				case "Nation":
					if (nationChannel == null) return;
					TBMCChatAPI.SendSystemMessage(nationChannel,
						new Channel.RecipientTestResult(TownyComponent.getTownNationIndex(groupID, true), groupID),
						message, target, ChatProcessing.MCORIGIN);
					break;
				case "Global":
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat,
						Channel.RecipientTestResult.ALL,
						message, target, ChatProcessing.MCORIGIN);
					break;
			}
		}
	};

	private static TBMCSystemChatEvent.BroadcastTarget target;
	private static Channel townChannel;
	private static Channel nationChannel;

	public static void setup(Channel townChannel, Channel nationChannel) {
		target = TBMCSystemChatEvent.BroadcastTarget.add("towny");
		TownyAnnouncer.townChannel = townChannel;
		TownyAnnouncer.nationChannel = nationChannel;
		((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getContext().getConfiguration().addAppender(HANDLER);
	}

	public static void setdown() {
		TBMCSystemChatEvent.BroadcastTarget.remove(target);
		target = null;
		TownyAnnouncer.townChannel = null;
		TownyAnnouncer.nationChannel = null;
		((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getContext().getConfiguration().getAppenders().remove(TownyAnnouncer.class.getSimpleName());
	}
}
