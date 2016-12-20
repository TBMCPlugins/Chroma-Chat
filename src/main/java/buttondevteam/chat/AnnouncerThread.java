package buttondevteam.chat;

import org.bukkit.Bukkit;

public class AnnouncerThread {
	private static int AnnounceMessageIndex = 0;

	public static void Run() {
		while (!PluginMain.Instance.stop) {
			try {
				Thread.sleep(PluginMain.AnnounceTime);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			if (PluginMain.AnnounceMessages.size() > AnnounceMessageIndex) {
				Bukkit.broadcastMessage(PluginMain.AnnounceMessages.get(AnnounceMessageIndex));
				AnnounceMessageIndex++;
				if (AnnounceMessageIndex == PluginMain.AnnounceMessages.size())
					AnnounceMessageIndex = 0;
			}
		}
	}
}
