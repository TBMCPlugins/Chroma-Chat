package buttondevteam.chat;

import org.bukkit.Bukkit;

public class AnnouncerThread implements Runnable {
	private static int AnnounceMessageIndex = 0;

	@Override
	public void run() {
		while (!PluginMain.Instance.stop) {
			try {
				Thread.sleep(PluginMain.AnnounceTime);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			if (Bukkit.getOnlinePlayers().size() == 0) continue; //Don't post to Discord if nobody is on
			if (PluginMain.AnnounceMessages.size() > AnnounceMessageIndex) {
				Bukkit.broadcastMessage(PluginMain.AnnounceMessages.get(AnnounceMessageIndex));
				AnnounceMessageIndex++;
				if (AnnounceMessageIndex == PluginMain.AnnounceMessages.size())
					AnnounceMessageIndex = 0;
			}
		}
	}
}
