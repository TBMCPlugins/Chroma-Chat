package buttondevteam.chat.listener;

import buttondevteam.chat.PluginMain;
import com.earth2me.essentials.User;
import com.palmergames.bukkit.towny.event.*;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownyListener implements Listener {
    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        val clrs = PluginMain.TownColors.remove(event.getOldName().toLowerCase());
        if (clrs != null)
            PluginMain.TownColors.put(event.getTown().getName().toLowerCase(), clrs);
    }

    @EventHandler //Gets called on town load as well
    public void onTownJoin(TownAddResidentEvent event) {
        Player p = Bukkit.getPlayer(event.getResident().getName());
        if (p != null)
            PlayerJoinLeaveListener.updatePlayerColors(p);
    }

    @EventHandler
    public void onTownLeave(TownRemoveResidentEvent event) {
        Player p = Bukkit.getPlayer(event.getResident().getName());
        if (p != null) {
            User user = PluginMain.essentials.getUser(p);
            user.setNickname(ChatColor.stripColor(user.getNick(true).replace("~", "")));
        }
    }

    @EventHandler
    public void onTownDelete(DeleteTownEvent event) {
        PluginMain.TownColors.remove(event.getTownName().toLowerCase());
    }

    @EventHandler
    public void onTownCreate(NewTownEvent event) {
        Player p = Bukkit.getPlayer(event.getTown().getMayor().getName());
        if (p != null)
            p.sendMessage("§6Use /u towncolor <color1> [color2] to set a color for the town.");
    }
}
