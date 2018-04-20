package buttondevteam.chat.listener;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.FlairStates;
import buttondevteam.chat.PlayerJoinTimerTask;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.UnlolCommand;
import buttondevteam.lib.player.TBMCPlayerJoinEvent;
import buttondevteam.lib.player.TBMCPlayerLoadEvent;
import buttondevteam.lib.player.TBMCPlayerSaveEvent;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

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
		Player p = Bukkit.getPlayer(cp.getUUID());

		if (!cp.FlairState().get().equals(FlairStates.NoComment)) {
			PluginMain.ConfirmUserMessage(cp);
			Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
				@Override
				public void run() {
                    mp.FlairUpdate();
				}
			};
			tt.mp = cp;
			timer.schedule(tt, 1000);
		} else {
			/*Timer timer = new Timer();
			PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {

				@Override
				public void run() {
					Player player = Bukkit.getPlayer(mp.PlayerName().get());
					if (player == null)
						return;

					if (mp.FlairState().get().equals(FlairStates.NoComment)) {
						String json = String.format(
								"[\"\",{\"text\":\"If you're from Reddit and you'd like your /r/TheButton flair displayed ingame, write your Minecraft name to \",\"color\":\"aqua\"},{\"text\":\"[this thread].\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click here to go to the Reddit thread\",\"color\":\"aqua\"}]}}}]",
								PluginMain.FlairThreadURL);
						PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
								"tellraw " + mp.PlayerName() + " " + json);
						json = "[\"\",{\"text\":\"If you aren't from Reddit or don't want the flair, type /u ignore to prevent this message after next login.\",\"color\":\"aqua\"}]";
						PluginMain.Instance.getServer().dispatchCommand(PluginMain.Console,
								"tellraw " + mp.PlayerName() + " " + json);
					}
				}
			};
			tt.mp = cp;
			timer.schedule(tt, 15 * 1000);*/ //TODO: Better Reddit integration (OAuth)
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

        Bukkit.getScheduler().runTaskLater(PluginMain.Instance, () -> {
            updatePlayerColors(p, cp); //TODO: Doesn't have effect
        }, 5);

		if (cp.ChatOnly || p.getGameMode().equals(GameMode.SPECTATOR)) {
			cp.ChatOnly = false;
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

	@EventHandler
	public void onPlayerSave(TBMCPlayerSaveEvent e) {
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		PlayerListener.nicknames.inverse().remove(event.getPlayer().getUniqueId());
		UnlolCommand.Lastlol.values().removeIf(lld -> lld.getLolowner().equals(event.getPlayer()));
	}

    private static String getPlayerNickname(Player player, User user) {
        String nickname = user.getNick(true);
        if (nickname.contains("~")) //StartsWith doesn't work because of color codes
            nickname = nickname.replace("~", ""); //It gets stacked otherwise
        val res = PluginMain.TU.getResidentMap().get(player.getName().toLowerCase());
        if (res == null || !res.hasTown())
            return nickname;
        try {
            val clrs = PluginMain.TownColors.get(res.getTown().getName().toLowerCase());
            if (clrs == null)
                return nickname;
            StringBuilder ret = new StringBuilder();
            String name = ChatColor.stripColor(nickname);
            AtomicInteger prevlen = new AtomicInteger();
            BiFunction<Integer, Integer, String> coloredNamePart = (len, i) -> "§"
                    + Integer.toHexString(clrs[i].ordinal()) // 'Odds' are the last character is chopped off so we make sure to include all chars at the end
                    + (i + 1 == clrs.length ? name.substring(prevlen.get())
                    : name.substring(prevlen.get(), prevlen.addAndGet(len)));
            int len = name.length() / clrs.length;
            val nclar = ChatPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class).NameColorLocations().get();
            int[] ncl = nclar == null ? null : nclar.stream().mapToInt(Integer::intValue).toArray();
            if (ncl != null && (Arrays.stream(ncl).sum() != name.length() || ncl.length != clrs.length))
                ncl = null; // Reset if name length changed
            for (int i = 0; i < clrs.length; i++)
                ret.append(coloredNamePart.apply(ncl == null ? len : ncl[i], i));
            return ret.toString();
        } catch (NotRegisteredException e) {
            return nickname;
        }
    }

    public static void updatePlayerColors(Player player) { //Probably while ingame (/u ncolor)
        updatePlayerColors(player, ChatPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class));
    }

    public static void updatePlayerColors(Player player, ChatPlayer cp) { //Probably at join - nop, nicknames
        User user = PluginMain.essentials.getUser(player);
        user.setNickname(getPlayerNickname(player, user));
        user.setDisplayNick(); //These won't fire the nick change event
        cp.FlairUpdate(); //Update in list
    }
}
