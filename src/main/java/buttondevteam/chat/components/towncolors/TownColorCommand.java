package buttondevteam.chat.components.towncolors;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.CustomTabCompleteMethod;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.entity.Player;

@CommandClass(helpText = {
	"Town Color", //
	"This command allows setting a color for a town.", //
	"The town will be shown with this color on Dynmap and all players in the town will appear in chat with these colors.", //
	"The colors will split the name evenly but residents can override that with /u ncolor.", //
}) // TODO: /u u when annotation not present
@RequiredArgsConstructor
public class TownColorCommand extends UCommandBase {
	private final TownColorComponent component;

	@Command2.Subcommand
	public boolean def(Player player, String... colornames) {
		String msg = "§cYou need to be the mayor of a town to set its colors.";
		try {
			Resident res = TownyComponent.dataSource.getResident(player.getName());
			if (!res.isMayor()) {
				player.sendMessage(msg);
				return true;
			}
			val cc = component.colorCount.get();
			if (colornames.length > cc) {
				player.sendMessage("You can only use " + cc + " color" + (cc > 1 ? "s" : "") + ".");
				return true;
			}
			final Town t = res.getTown();
			return buttondevteam.chat.components.towncolors.admin.TownColorCommand.SetTownColor(player, t, colornames);
		} catch (NotRegisteredException e) {
			player.sendMessage(msg);
			return true;
		}
	}

	@CustomTabCompleteMethod(param = "colornames")
	public Iterable<String> def() {
		return buttondevteam.chat.components.towncolors.admin.TownColorCommand.tabcompleteColor();
	}
}
