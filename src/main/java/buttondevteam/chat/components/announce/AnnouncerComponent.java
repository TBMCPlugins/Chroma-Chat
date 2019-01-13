package buttondevteam.chat.components.announce;

import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class AnnouncerComponent extends Component implements Runnable {
	public ConfigData<ArrayList<String>> AnnounceMessages() {
		return getConfig().getData("announceMessages", new ArrayList<>(0));
	}

	public ConfigData<Integer> AnnounceTime() {
		return getConfig().getData("announceTime", 15 * 60 * 1000);
	}
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
				Bukkit.broadcastMessage(AnnounceMessages().get().get(AnnounceMessageIndex));
				AnnounceMessageIndex++;
				if (AnnounceMessageIndex == AnnounceMessages().get().size())
					AnnounceMessageIndex = 0;
			}
		}
	}

	@Override
	protected void enable() {
		registerCommand(new AddCommand());
		registerCommand(new EditCommand());
		registerCommand(new ListCommand());
		registerCommand(new RemoveCommand());
		registerCommand(new SetTimeCommand());
		new Thread(this).start();
	}

	@Override
	protected void disable() {
	}
}
