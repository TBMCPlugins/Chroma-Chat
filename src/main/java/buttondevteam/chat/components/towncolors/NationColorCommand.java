package buttondevteam.chat.components.towncolors;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;

@CommandClass(helpText = {
	"Nation Color", //
	"This command allows setting a color for a nation.", //
	"Each town in the nation will have it's first color (border) set to this color.", //
	"See the help text for /u towncolor for more details.", //
})
public class NationColorCommand extends UCommandBase {
	@Command2.Subcommand
	public boolean def(Player player, String color) {
		Resident res;
		if (!(TownyComponent.TU.getResidentMap().containsKey(player.getName().toLowerCase())
			&& (res = TownyComponent.TU.getResidentMap().get(player.getName().toLowerCase())).isKing())) {
			player.sendMessage("§cYou need to be the king of a nation to set it's colors.");
			return true;
		}
		final Nation n;
		try {
			n = res.getTown().getNation();
		} catch (NotRegisteredException e) {
			TBMCCoreAPI.SendException("Failed to set nation color for player " + player + "!", e);
			player.sendMessage("§cCouldn't find your town/nation... Error reported.");
			return true;
		}
		return buttondevteam.chat.components.towncolors.admin.NationColorCommand.SetNationColor(player, n, color);
	}
}
