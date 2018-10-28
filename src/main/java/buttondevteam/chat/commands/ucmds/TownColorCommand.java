package buttondevteam.chat.commands.ucmds;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.OptionallyPlayerCommandClass;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Player;

@CommandClass // TODO: /u u when annotation not present
@OptionallyPlayerCommandClass(playerOnly = true)
public class TownColorCommand extends UCommandBase {
	@Override
	public String GetHelpText(String alias)[] {
		StringBuilder cns = new StringBuilder(" <colorname1>");
		for (int i = 2; i <= ColorCount; i++)
			cns.append(" [colorname").append(i).append("]");
		return new String[] { //
				"§6---- Town Color ----", //
				"This command allows setting a color for a town.", //
				"The town will be shown with this color on Dynmap and all players in the town will appear in chat with these colors.", //
				"The colors will split the name evenly.", //
				"Usage: /" + GetCommandPath() + cns, //
				"Example: /" + GetCommandPath() + " blue" //
		};
	}

	public static byte ColorCount;

	@Override
	public boolean OnCommand(Player player, String alias, String[] args) {
		Resident res;
		if (!(PluginMain.TU.getResidentMap().containsKey(player.getName().toLowerCase())
				&& (res = PluginMain.TU.getResidentMap().get(player.getName().toLowerCase())).isMayor())) {
			player.sendMessage("§cYou need to be the mayor of a town to set it's colors.");
			return true;
		}
		if (args.length > ColorCount) {
			player.sendMessage("You can only use " + ColorCount + " color" + (ColorCount > 1 ? "s" : "") + ".");
			return true;
		}
		String[] a = new String[args.length + 1];
		System.arraycopy(args, 0, a, 1, args.length);
		try {
			a[0] = res.getTown().getName();
		} catch (NotRegisteredException e) {
			TBMCCoreAPI.SendException("Failed to set town color for player " + player + "!", e);
			player.sendMessage("§cCouldn't find your town... Error reported.");
			return true;
		}
		return buttondevteam.chat.commands.ucmds.admin.TownColorCommand.SetTownColor(player, alias, a);
	}
}
