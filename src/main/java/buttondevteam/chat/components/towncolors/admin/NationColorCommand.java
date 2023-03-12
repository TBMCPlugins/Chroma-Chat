package buttondevteam.chat.components.towncolors.admin;

import buttondevteam.chat.PluginMain;
import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase;
import buttondevteam.chat.components.towncolors.TownColorComponent;
import buttondevteam.chat.components.towncolors.TownyListener;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.CustomTabCompleteMethod;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandClass(helpText = {
	"Nation color", //
	"Sets the color of the nation.", //
})
public class NationColorCommand extends AdminCommandBase {
	@Command2.Subcommand
	public boolean def(CommandSender sender, String nation, String color) {
		final Nation n = TownyComponent.dataSource.getNation(nation);
		if (n == null) {
			sender.sendMessage("§cThe nation '" + nation + "' cannot be found.");
			return true;
		}
		return SetNationColor(sender, n, color);
	}

	@CustomTabCompleteMethod(param = "color")
	public Iterable<String> def(String nation) {
		return TownColorCommand.tabcompleteColor();
	}

	@CustomTabCompleteMethod(param = "nation")
	public Iterable<String> def() {
		return TownyComponent.dataSource.getNations().stream().map(TownyObject::getName)::iterator;
	}

	public static boolean SetNationColor(CommandSender sender, Nation nation, String color) {
		val c = TownColorCommand.getColorOrSendError(color, sender);
		if (!c.isPresent()) return true;
		if (!c.get().getName().equals(Color.White.getName())) { //Default nation color
			for (val e : TownColorComponent.NationColor.entrySet()) {
				if (e.getValue().getName().equals(c.get().getName())) {
					sender.sendMessage("§cThe nation " + e.getKey() + " already uses this color!");
					return true;
				}
			}
		}
		TownColorComponent.NationColor.put(nation.getName().toLowerCase(), c.get());
		Bukkit.getScheduler().runTaskAsynchronously(PluginMain.Instance, () -> {
			for (Town t : nation.getTowns())
				TownyListener.updateTownMembers(t);
		});
		sender.sendMessage("§bNation color set to " + TownColorCommand.getColorText(c.get()));
		return true;
	}
}
