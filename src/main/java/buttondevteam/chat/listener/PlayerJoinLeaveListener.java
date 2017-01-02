package buttondevteam.chat.listener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.earth2me.essentials.Essentials;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.FlairStates;
import buttondevteam.chat.PlayerJoinTimerTask;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.player.TBMCPlayerAddEvent;
import buttondevteam.lib.player.TBMCPlayerJoinEvent;
import buttondevteam.lib.player.TBMCPlayerLoadEvent;
import buttondevteam.lib.player.TBMCPlayerSaveEvent;

public class PlayerJoinLeaveListener implements Listener {

	@EventHandler
	public void onPlayerLoad(TBMCPlayerLoadEvent e) {
		ChatPlayer cp = e.GetPlayer().asPluginPlayer(ChatPlayer.class);
		cp.FlairUpdate();
	}

	@EventHandler
	public void onPlayerTBMCJoin(TBMCPlayerJoinEvent e) {
		if (PluginMain.essentials == null)
			PluginMain.essentials = ((Essentials) Bukkit.getPluginManager().getPlugin("Essentials"));
		ChatPlayer cp = e.GetPlayer().asPluginPlayer(ChatPlayer.class);
		Player p = Bukkit.getPlayer(cp.getUuid());

		if (!cp.getFlairState().equals(FlairStates.NoComment)) {
			PluginMain.ConfirmUserMessage(cp);
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
					p.setPlayerListName(p.getName() + mp.GetFormattedFlair());
				}
			};
			tt.mp = cp;
			timer.schedule(tt, 1000);
		} else {
			if (cp.getFlairTime() == 0x00)
				cp.SetFlair(ChatPlayer.FlairTimeNone);
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {

				@Override
				public void run() {
					Player player = Bukkit.getPlayer(mp.getPlayerName());
					if (player == null)
						return;

					if (mp.getFlairState().equals(FlairStates.NoComment)) {
						String json = String.format(
								"[\"\",{\"text\":\"If you're from Reddit and you'd like your /r/TheButton flair displayed ingame, write your Minecraft name to \",\"color\":\"aqua\"},{\"text\":\"[this thread].\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread\",\"color\":\"aqua\"}]}}}]",
								PluginMain.FlairThreadURL);
						PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
								"tellraw " + mp.getPlayerName() + " " + json);
						json = "[\"\",{\"text\":\"If you aren't from Reddit or don't want the flair, type /u ignore to prevent this message after next login.\",\"color\":\"aqua\"}]";
						PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
								"tellraw " + mp.getPlayerName() + " " + json);
					}
				}
			};
			tt.mp = cp;
			timer.schedule(tt, 15 * 1000);
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
		PlayerListener.nicknames.put(nwithoutformatting, p.getUniqueId());

		cp.RPMode = true;

		cp.FlairUpdate();

		if (cp.ChatOnly || p.getGameMode().equals(GameMode.SPECTATOR)) {
			cp.ChatOnly = false;
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

	@EventHandler
	public void onPlayerSave(TBMCPlayerSaveEvent e) {
	}

	@EventHandler
	public void onPlayerAdd(TBMCPlayerAddEvent event) {
		ChatPlayer cp = event.GetPlayer().asPluginPlayer(ChatPlayer.class);
		cp.SetFlair(ChatPlayer.FlairTimeNone);
		cp.setFlairState(FlairStates.NoComment);
		cp.setUserNames(new ArrayList<>());
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		String deletenick = null;
		for (String nickname : PlayerListener.nicknames.keySet()) {
			UUID uuid = PlayerListener.nicknames.get(nickname);
			if (event.getPlayer().getUniqueId().equals(uuid)) {
				deletenick = nickname;
				break;
			}
		}
		if (deletenick != null)
			PlayerListener.nicknames.remove(deletenick);
	}

}
