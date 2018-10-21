package buttondevteam.chat.listener;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.FlairStates;
import buttondevteam.chat.PlayerJoinTimerTask;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.UnlolCommand;
import buttondevteam.lib.chat.Color;
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
		} //TODO: Better Reddit integration (OAuth)

		String nwithoutformatting = PluginMain.essentials.getUser(p).getNickname();

		int index;
		if (nwithoutformatting != null) {
			while ((index = nwithoutformatting.indexOf("§k")) != -1)
				nwithoutformatting = nwithoutformatting.replace("§k" + nwithoutformatting.charAt(index + 2), ""); // Support for one random char
			while ((index = nwithoutformatting.indexOf('§')) != -1)
				nwithoutformatting = nwithoutformatting.replace("§" + nwithoutformatting.charAt(index + 1), "");
		} else
			nwithoutformatting = p.getName();
		PlayerListener.nicknames.forcePut(nwithoutformatting.toLowerCase(), p.getUniqueId());

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

	private static String getPlayerNickname(Player player, User user, ChatPlayer cp) {
        String nickname = user.getNick(true);
        if (nickname.contains("~")) //StartsWith doesn't work because of color codes
            nickname = nickname.replace("~", ""); //It gets stacked otherwise
		String name = ChatColor.stripColor(nickname); //Enforce "town colors" on non-members
        val res = PluginMain.TU.getResidentMap().get(player.getName().toLowerCase());
        if (res == null || !res.hasTown())
	        return name;
        try {
            val clrs = PluginMain.TownColors.get(res.getTown().getName().toLowerCase());
            if (clrs == null)
	            return name;
            StringBuilder ret = new StringBuilder();
            AtomicInteger prevlen = new AtomicInteger();
	        BiFunction<Color, Integer, String> anyColoredNamePart = (c, len) -> "§" //Len==0 if last part
			        + Integer.toHexString(c.ordinal()) // 'Odds' are the last character is chopped off so we make sure to include all chars at the end
			        + (len == 0 ? name.substring(prevlen.get())
			        : name.substring(prevlen.get(), prevlen.addAndGet(len)));
	        BiFunction<Integer, Integer, String> coloredNamePart = (len, i)
			        -> anyColoredNamePart.apply(clrs[i], i + 1 == clrs.length ? 0 : len);
	        final int len = name.length() / (clrs.length + 1); //The above param is needed because this isn't always passed
	        Color nc;
	        /*if(res.getTown().hasNation()
			        &&(nc=PluginMain.NationColor.get(res.getTown().getNation().getName().toLowerCase()))!=null)
	        	len = name.length() / (clrs.length+1);
	        else
	        	len = name.length() / clrs.length;*/
	        val nclar = cp.NameColorLocations().get();
            int[] ncl = nclar == null ? null : nclar.stream().mapToInt(Integer::intValue).toArray();
            if (ncl != null && (Arrays.stream(ncl).sum() != name.length() || ncl.length != clrs.length))
                ncl = null; // Reset if name length changed
            //System.out.println("ncl: "+Arrays.toString(ncl)+" - sum: "+Arrays.stream(ncl).sum()+" - name len: "+name.length());
	        if (!res.getTown().hasNation()
			        || (nc = PluginMain.NationColor.get(res.getTown().getNation().getName().toLowerCase())) == null)
		        nc = Color.White;
	        ret.append(anyColoredNamePart.apply(nc, ncl == null ? len : ncl[0])); //Make first color the nation color
            for (int i = 0; i < clrs.length; i++)
	            //ret.append(coloredNamePart.apply(ncl == null ? len : (nc==null?ncl[i]:ncl[i+1]), i));
	            ret.append(coloredNamePart.apply(ncl == null ? len : ncl[i + 1], i));
            return ret.toString();
        } catch (NotRegisteredException e) {
            return nickname;
        }
    }

    public static void updatePlayerColors(Player player) { //Probably while ingame (/u ncolor)
        updatePlayerColors(player, ChatPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class));
    }

	@SuppressWarnings("WeakerAccess")
	public static void updatePlayerColors(Player player, ChatPlayer cp) { //Probably at join - nop, nicknames
		User user = PluginMain.essentials.getUser(player);
		user.setNickname(getPlayerNickname(player, user, cp));
		user.setDisplayNick(); //These won't fire the nick change event
		cp.FlairUpdate(); //Update in list
	}
}
