package buttondevteam.chat.commands.ucmds;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.OptionallyPlayerCommandClass;
import gnu.trove.list.array.TIntArrayList;

@OptionallyPlayerCommandClass(playerOnly = true)
@CommandClass
public class NColorCommand extends UCommandBase {
	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { //
				"§6---- Name color ----", //
				"This command allows you to set how the town colors look on your name.", //
				"To use this command, you need to be in a town which has town colors set.", //
				"Use a vertical line as a separator between the colors.", //
				"Example: /u ncolor Norbi|Peti --> §6Norbi§ePeti" //
		};
	}

	@Override
	public boolean OnCommand(Player player, String alias, String[] args) {
		Resident res;
		Town town;
		try {
			if ((res = PluginMain.TU.getResidentMap().get(player.getName().toLowerCase())) == null || !res.hasTown()
					|| (town = res.getTown()) == null) {
				player.sendMessage("§cYou need to be in a town.");
				return true;
			}
		} catch (Exception e) {
			player.sendMessage("§cYou need to be in a town. (" + e + ")");
			return true;
		}
		if (args.length == 0)
			return false;
		if (!args[0].replace("|", "").equalsIgnoreCase(ChatColor.stripColor(player.getDisplayName()))) {
			player.sendMessage("§cThe name you gave doesn't match your name. Make sure to use "
					+ ChatColor.stripColor(player.getDisplayName()) + "§c with added vertical lines (|).");
			return true;
		}
		String[] nameparts = args[0].split("\\|");
		Color[] towncolors = PluginMain.TownColors.get(town.getName().toLowerCase());
		if (towncolors == null) {
			player.sendMessage("§cYour town doesn't have a color set. The town mayor can set it using /u towncolor.");
			return true;
		}
		if (nameparts.length < towncolors.length) {
			player.sendMessage("§cYou need more vertical lines (|) in your name.");
			return true;
		}
		if (nameparts.length > towncolors.length * 2) {
			player.sendMessage("§cYou have waay too many vertical lines (|) in your name.");
			return true;
		}
		if (nameparts.length > towncolors.length) {
			player.sendMessage("§cYou have too many vertical lines (|) in your name.");
			return true;
		}
		ChatPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class).NameColorLocations()
				.set(TIntArrayList.wrap(Arrays.stream(nameparts).mapToInt(np -> np.length()).toArray())); // No byte[]
		player.sendMessage("§bName colors set."); // TODO: ArrayList is what it becomes I think
		return true;
	}
}
