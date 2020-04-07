package buttondevteam.chat.components.towncolors;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.chat.components.towncolors.admin.TownColorCommand;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.CustomTabCompleteMethod;
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
		String msg = "§cYou need to be the king of a nation to set it's colors.";
		try {
			Resident res = TownyComponent.dataSource.getResident(player.getName());
			if (!res.isKing()) {
				player.sendMessage(msg);
				return true;
			}
			final Nation n = res.getTown().getNation();
			return buttondevteam.chat.components.towncolors.admin.NationColorCommand.SetNationColor(player, n, color);
		} catch (NotRegisteredException e) {
			player.sendMessage(msg);
			return true;
		}
	}

	@CustomTabCompleteMethod(param = "color")
	public Iterable<String> def() {
		return TownColorCommand.tabcompleteColor();
	}
}
