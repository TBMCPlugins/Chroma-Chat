package buttondevteam.chat.components.announce;

import buttondevteam.chat.PluginMain;
import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.chat.TBMCChatAPI;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class AnnouncerComponent extends Component<PluginMain> implements Runnable {
	public ConfigData<ArrayList<String>> AnnounceMessages() {
		return getConfig().getData("announceMessages", new ArrayList<>(0));
	}

	public ConfigData<Integer> AnnounceTime() {
		return getConfig().getData("announceTime", 15 * 60 * 1000);
	}

	private TBMCSystemChatEvent.BroadcastTarget target;

	private static int AnnounceMessageIndex = 0;

	@Override
	public void run() {
		while (isEnabled()) {
			try {
				Thread.sleep(AnnounceTime().get());
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			if (Bukkit.getOnlinePlayers().size() == 0) continue; //Don't post to Discord if nobody is on
			if (AnnounceMessages().get().size() > AnnounceMessageIndex) {
				TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, AnnounceMessages().get().get(AnnounceMessageIndex), target);
				AnnounceMessageIndex++;
				if (AnnounceMessageIndex == AnnounceMessages().get().size())
					AnnounceMessageIndex = 0;
			}
		}
	}

	@Override
	protected void enable() {
		target= TBMCSystemChatEvent.BroadcastTarget.add("announcements");
		registerCommand(new AnnounceCommand(this));
		new Thread(this).start();
	}

	@Override
	protected void disable() {
	}
}
