package buttondevteam.chat.listener;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PlayerJoinTimerTask;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.ucmds.HistoryCommand;
import buttondevteam.chat.components.flair.FlairComponent;
import buttondevteam.chat.components.flair.FlairStates;
import buttondevteam.core.ComponentManager;
import buttondevteam.lib.player.TBMCPlayerBase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Timer;

public class PlayerJoinLeaveListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		ChatPlayer cp = TBMCPlayerBase.getPlayer(p.getUniqueId(), ChatPlayer.class);
		cp.FlairUpdate();

		if (ComponentManager.isEnabled(FlairComponent.class)) {
			if (!cp.FlairState.get().equals(FlairStates.NoComment)) {
				FlairComponent.ConfirmUserMessage(cp);
				Timer timer = new Timer();
				PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
					@Override
					public void run() {
						mp.FlairUpdate();
					}
				};
				tt.mp = cp;
				timer.schedule(tt, 1000);
			} //TODO: Better Reddit integration (OAuth)
		}

		String nwithoutformatting = PluginMain.essentials.getUser(p).getNickname();

		int index;
		if (nwithoutformatting != null) {
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replace("§k" + nwithoutformatting.charAt(index + 2), ""); // Support for one random char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replace("§" + nwithoutformatting.charAt(index + 1), "");
		} else
			nwithoutformatting = p.getName();
		PlayerListener.nicknames.forcePut(nwithoutformatting.toLowerCase(), p.getUniqueId()); //TODO: FormatterComponent

		if (PluginMain.Instance.storeChatHistory.get())
			HistoryCommand.showHistory(e.getPlayer(), null);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		PlayerListener.nicknames.inverse().remove(event.getPlayer().getUniqueId());
	}

}
