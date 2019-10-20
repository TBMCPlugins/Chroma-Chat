package buttondevteam.chat.components.towny;

import buttondevteam.chat.ChatUtils;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.chat.TBMCChatAPI;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.filter.LevelRangeFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.regex.Pattern;

public class TownyAnnouncer {
	private static final Pattern LOG_TYPE_PATTERN = Pattern.compile("\\[(\\w+) (?:Msg|Message)](?: (\\w+):)?");
	private static final String APPENDER_NAME = "Chroma";
	private static final AbstractAppender HANDLER = new AbstractAppender(APPENDER_NAME,
		LevelRangeFilter.createFilter(Level.INFO, Level.INFO, Filter.Result.ACCEPT, Filter.Result.ACCEPT),
		PatternLayout.createDefaultLayout()) {
		@Override
		public void append(LogEvent logRecord) {
			if (logRecord.getMessage() == null) return;
			String message = logRecord.getMessage().getFormattedMessage();
			val m = LOG_TYPE_PATTERN.matcher(message);
			if (!m.find()) return;
			String groupID = m.group(2); //The group ID is correctly cased
			switch (String.valueOf(m.group(1))) { //valueOf: Handles null
				case "Town":
					if (townChannel == null) return;
					TBMCChatAPI.SendSystemMessage(townChannel,
						new Channel.RecipientTestResult(TownyComponent.getTownNationIndex(groupID, false), groupID),
						message, target, ChatUtils.MCORIGIN);
					break;
				case "Nation":
					if (nationChannel == null) return;
					TBMCChatAPI.SendSystemMessage(nationChannel,
						new Channel.RecipientTestResult(TownyComponent.getTownNationIndex(groupID, true), groupID),
						message, target, ChatUtils.MCORIGIN);
					break;
				case "Global":
					TBMCChatAPI.SendSystemMessage(Channel.GlobalChat,
						Channel.RecipientTestResult.ALL,
						message, target, ChatUtils.MCORIGIN);
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
		/*System.out.println(LogManager.getLogger("com.palmergames.bukkit.towny"));
		((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getContext().getConfiguration().addAppender(HANDLER);
		System.out.println(((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getAppenders());
		((LoggerContext)LogManager.getContext(false)).updateLoggers();
		System.out.println(((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getAppenders());
		((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getContext().updateLoggers();
		System.out.println(((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getAppenders());*/
		val lc = (LoggerContext) LogManager.getContext(false);
		HANDLER.start();
		lc.getConfiguration().addAppender(HANDLER);
		Logger logger = lc.getLogger("com.palmergames.bukkit.towny");
		//System.out.println(logger);
		//System.out.println(lc.getConfiguration().<Appender>getAppender(HANDLER.getName())); //T defaults to String because of the context which results in a cast exception
		logger.addAppender(lc.getConfiguration().getAppender(HANDLER.getName()));
		logger.get().addAppender(HANDLER, Level.INFO, HANDLER.getFilter());
		lc.updateLoggers();
		//System.out.println(logger.getAppenders());
	}

	public static void setdown() {
		TBMCSystemChatEvent.BroadcastTarget.remove(target);
		target = null;
		TownyAnnouncer.townChannel = null;
		TownyAnnouncer.nationChannel = null;
		((Logger) LogManager.getLogger("com.palmergames.bukkit.towny")).getAppenders().remove(APPENDER_NAME);
	}
}
