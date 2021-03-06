package buttondevteam.chat.components.announce;

import buttondevteam.chat.PluginMain;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ComponentMetadata;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.architecture.ListConfigData;
import buttondevteam.lib.chat.TBMCChatAPI;
import org.bukkit.Bukkit;

/**
 * Displays the configured messages at the set interval when someone is online.
 */
@ComponentMetadata(enabledByDefault = false)
public class AnnouncerComponent extends Component<PluginMain> implements Runnable {
	/**
	 * The messages to display to players.
	 */
	public ListConfigData<String> announceMessages = getConfig().getListData("announceMessages");

	/**
	 * The time in milliseconds between the messages. Use /u announce settime to set minutes.
	 */
	public ConfigData<Integer> announceTime = getConfig().getData("announceTime", 15 * 60 * 1000);

	private TBMCSystemChatEvent.BroadcastTarget target;

	private int AnnounceMessageIndex = 0;

	@Override
	public void run() {
		while (isEnabled()) {
			try {
				Thread.sleep(announceTime.get());
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			if (Bukkit.getOnlinePlayers().size() == 0) continue; //Don't post to Discord if nobody is on
			if (announceMessages.get().size() > AnnounceMessageIndex) {
				TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, announceMessages.get().get(AnnounceMessageIndex), target);
				AnnounceMessageIndex++;
				if (AnnounceMessageIndex == announceMessages.get().size())
					AnnounceMessageIndex = 0;
			}
		}
	}

	@Override
	protected void enable() {
		target = TBMCSystemChatEvent.BroadcastTarget.add("announcements");
		registerCommand(new AnnounceCommand(this));
		new Thread(this).start();
	}

	@Override
	protected void disable() {
	}
}
