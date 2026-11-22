package buttondevteam.chat.components.towncolors.admin;

import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase;
import buttondevteam.chat.components.towncolors.TownColorComponent;
import buttondevteam.chat.components.towncolors.TownyListener;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.CustomTabCompleteMethod;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandClass(helpText = {
	"Town Color", //
	"This command allows setting a color for a town.", //
	"The town will be shown with this color on Dynmap and all players in the town will appear in chat with these colors.", //
	"The colors will split the name evenly.", //
})
public class TownColorCommand extends AdminCommandBase {
	@Command2.Subcommand
	public boolean def(CommandSender sender, String town, String... colornames) {
		if (TownyComponent.dataSource.getTown(town) == null) {
			sender.sendMessage("§cThe town '" + town + "' cannot be found.");
			return true;
		}
		Town targetTown = TownyComponent.dataSource.getTown(town);
		if (targetTown == null) {
			sender.sendMessage("§cThe town '" + town + "' cannot be found.");
			return true;
		}
		return SetTownColor(sender, targetTown, colornames);
	}

	@CustomTabCompleteMethod(param = "colornames")
	public Iterable<String> def(String town) {
		return TownColorCommand.tabcompleteColor();
	}

	@CustomTabCompleteMethod(param = "town")
	public Iterable<String> def() {
		return TownyComponent.dataSource.getTowns().stream().map(TownyObject::getName)::iterator;
	}

	public static boolean SetTownColor(CommandSender sender, Town town, String[] colors) {
		Color[] clrs = new Color[colors.length];
		for (int i = 0; i < colors.length; i++) {
			val c = getColorOrSendError(colors[i], sender);
			if (!c.isPresent())
				return true;
			clrs[i] = c.get();
		}
		Color tnc;
		boolean usenc = TownColorComponent.getComponent().useNationColors.get();
		if (usenc) {
			try {
				tnc = TownColorComponent.NationColor.get(town.getNation().getName().toLowerCase());
			} catch (Exception e) {
				tnc = null;
			}
			if (tnc == null) tnc = Color.White; //Default nation color - TODO: Make configurable
		} else tnc = null;
		for (Map.Entry<String, Color[]> other : TownColorComponent.TownColors.entrySet()) {
			Color nc;
			if (usenc) {
				try {
					nc = TownColorComponent.NationColor.get(TownyComponent.dataSource.getTown(other.getKey()).getNation().getName().toLowerCase());
				} catch (Exception e) { //Too lazy for lots of null-checks and it may throw exceptions anyways
					nc = null;
				}
				if (nc == null) nc = Color.White; //Default nation color
			} else nc = null;
			if (!usenc || nc.getName().equals(tnc.getName())) {
				int C = 0;
				if (clrs.length == other.getValue().length)
					for (int i = 0; i < clrs.length; i++)
						if (clrs[i].getName().equals(other.getValue()[i].getName()))
							C++;
						else break;
				if (C == clrs.length) {
					sender.sendMessage("§cThis town color combination is already used!");
					return true;
				}
			}
		}
		TownColorComponent.TownColors.put(town.getName().toLowerCase(), clrs);
		TownyListener.updateTownMembers(town);

		val dtp = Bukkit.getPluginManager().getPlugin("Dynmap-Towny");
		if (dtp != null) //If it's not found then it's not loaded, it'll be noticed by the admins if needed
			TownColorComponent.setTownColor(dtp, town.getName(), clrs, tnc);
		sender.sendMessage("§bColor(s) set.");
		return true;
	}

	public static Optional<Color> getColorOrSendError(String name, CommandSender sender) {
		val c = Arrays.stream(Color.values()).skip(1).filter(cc -> cc.getName().equalsIgnoreCase(name)).findAny();
		if (!c.isPresent()) { //^^ Skip black
			sender.sendMessage("§cThe color '" + name + "' cannot be found."); //ˇˇ Skip black
			sender.sendMessage("§cAvailable colors: " + Arrays.stream(Color.values()).skip(1).map(TownColorCommand::getColorText).collect(Collectors.joining(", ")));
			sender.sendMessage("§cMake sure to type them exactly as shown above.");
		}
		return c;
	}

	public static String getColorText(Color col) {
		return String.format("§%x%s§r", col.ordinal(), col.getName());
	}

	public static String getTownNameCased(String name) {
		val town = TownyComponent.dataSource.getTown(name);
		if (town == null) {
			return null;
		}
		return town.getName();
	}

	public static Iterable<String> tabcompleteColor() {
		return Arrays.stream(Color.values()).skip(1).map(Color::getName)::iterator;
	}
}
