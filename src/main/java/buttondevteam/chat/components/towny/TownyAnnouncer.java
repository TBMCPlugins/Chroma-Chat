package buttondevteam.chat.components.towny;

import buttondevteam.chat.ChatProcessing;
import buttondevteam.chat.PluginMain;
import buttondevteam.core.component.channel.Channel;
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
					TBMCChatAPI.SendSystemMessage(PluginMain.TownChat,
						new Channel.RecipientTestResult(TownyComponent.getTownNationIndex(groupID, false), groupID),
						logRecord.getMessage(), ChatProcessing.MCORIGIN);
					break;
				case "Nation":
					TBMCChatAPI.SendSystemMessage(PluginMain.NationChat,
						new Channel.RecipientTestResult(TownyComponent.getTownNationIndex(groupID, true), groupID),
						logRecord.getMessage(), ChatProcessing.MCORIGIN);
					break;
				case "Global":
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat,
						Channel.RecipientTestResult.ALL,
						logRecord.getMessage(), ChatProcessing.MCORIGIN);
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

	public static void setup() {
		TownyLogger.log.addHandler(HANDLER);
	}

	public static void setdown() {
		TownyLogger.log.removeHandler(HANDLER);
	}
}
