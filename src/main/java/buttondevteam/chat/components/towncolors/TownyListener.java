package buttondevteam.chat.components.towncolors;

import buttondevteam.chat.PluginMain;
import com.earth2me.essentials.User;
import com.palmergames.bukkit.towny.event.*;
import com.palmergames.bukkit.towny.object.Town;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class TownyListener implements Listener {
	@EventHandler
	public void onTownRename(RenameTownEvent event) {
		val clrs = TownColorComponent.TownColors.remove(event.getOldName().toLowerCase());
		if (clrs != null)
			TownColorComponent.TownColors.put(event.getTown().getName().toLowerCase(), clrs);
	}

	@EventHandler //Gets called on town load as well
	public void onTownJoin(TownAddResidentEvent event) {
		Player p = Bukkit.getPlayer(event.getResident().getName());
		if (p != null)
			TownColorComponent.updatePlayerColors(p);
	}

	public static void updateTownMembers(Town town) {
		town.getResidents().stream().map(r -> Bukkit.getPlayer(r.getName()))
			.filter(Objects::nonNull).forEach(TownColorComponent::updatePlayerColors);
	}

	@EventHandler
	public void onTownLeave(TownRemoveResidentEvent event) {
		Player p = Bukkit.getPlayer(event.getResident().getName());
		if (p != null)
			resetNameColor(p);
	}

	private void resetNameColor(Player p) {
		User user = PluginMain.essentials.getUser(p);
		user.setNickname(ChatColor.stripColor(user.getNick(true).replace("~", "")));
	}

	@EventHandler
	public void onTownDelete(DeleteTownEvent event) {
		TownColorComponent.TownColors.remove(event.getTownName().toLowerCase());
	}

	@EventHandler
	public void onTownCreate(NewTownEvent event) {
		Player p = Bukkit.getPlayer(event.getTown().getMayor().getName());
		if (p != null)
			p.sendMessage("§6Use /u towncolor to set a color for the town.");
	}

	//-----------------------------------------------------------------------------

	@EventHandler
	public void onNationRename(RenameNationEvent event) {
		val clrs = TownColorComponent.NationColor.remove(event.getOldName().toLowerCase());
		if (clrs != null)
			TownColorComponent.NationColor.put(event.getNation().getName().toLowerCase(), clrs);
	}

	@EventHandler //Gets called on town load as well
	public void onNationJoin(NationAddTownEvent event) {
		updateTownMembers(event.getTown());
	}

	@EventHandler
	public void onNationLeave(NationRemoveTownEvent event) {
		updateTownMembers(event.getTown()); //The town still has it's colours
	}

	@EventHandler
	public void onNationDelete(DeleteNationEvent event) {
		TownColorComponent.NationColor.remove(event.getNationName().toLowerCase());
	}

	@EventHandler
	public void onNationCreate(NewNationEvent event) {
		Player p = Bukkit.getPlayer(event.getNation().getCapital().getMayor().getName());
		if (p != null)
			p.sendMessage("§6Use /u nationcolor to set a color for the nation.");
	}
}
