package buttondevteam.chat.commands.ucmds;

import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.OptionallyPlayerCommandClass;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;

@CommandClass
@OptionallyPlayerCommandClass(playerOnly = true)
public class NationColorCommand extends UCommandBase {
	@Override
	public String[] GetHelpText(String alias) {
		return new String[]{ //
				"§6---- Nation Color ----", //
				"This command allows setting a color for a nation.", //
				"Each town in the nation will have it's first color (border) set to this color.", //
				"See the help text for /u towncolor for more details.", //
				"Usage: /" + GetCommandPath() + " <colorname>", //
				"Example: /" + GetCommandPath() + " blue" //
		};
	}

	@Override
	public boolean OnCommand(Player player, String alias, String[] args) {
		Resident res;
		if (!(TownyComponent.TU.getResidentMap().containsKey(player.getName().toLowerCase())
			&& (res = TownyComponent.TU.getResidentMap().get(player.getName().toLowerCase())).isKing())) {
			player.sendMessage("§cYou need to be the king of a nation to set it's colors.");
			return true;
		}
		if (args.length > 1) {
			player.sendMessage("You can only use one color.");
			return true;
		}
		String[] a = new String[args.length + 1];
		System.arraycopy(args, 0, a, 1, args.length);
		try {
			a[0] = res.getTown().getNation().getName();
		} catch (NotRegisteredException e) {
			TBMCCoreAPI.SendException("Failed to set nation color for player " + player + "!", e);
			player.sendMessage("§cCouldn't find your town/nation... Error reported.");
			return true;
		}
		return buttondevteam.chat.commands.ucmds.admin.NationColorCommand.SetNationColor(player, alias, a);
	}
}
