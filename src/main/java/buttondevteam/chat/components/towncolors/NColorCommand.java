package buttondevteam.chat.components.towncolors;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.chat.components.towny.TownyComponent;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@CommandClass(helpText = {
	"Name color", //
	"This command allows you to set how the town colors look on your name.", //
	"To use this command, you need to be in a town which has town colors set.", //
	"Use a vertical line (or a colon) as a separator between the colors.", //
	"Example: /u ncolor Norbi|Peti --> §6Norbi§ePeti" //
})
public class NColorCommand extends UCommandBase {
	@Command2.Subcommand
	public boolean def(Player player, String nameWithLines) {
		Resident res;
		Town town;
		try {
			if ((res = TownyComponent.TU.getResidentMap().get(player.getName().toLowerCase())) == null || !res.hasTown()
					|| (town = res.getTown()) == null) {
				player.sendMessage("§cYou need to be in a town.");
				return true;
			}
		} catch (Exception e) {
			player.sendMessage("§cYou need to be in a town. (" + e + ")");
			return true;
		}
		final String name = ChatColor.stripColor(player.getDisplayName()).replace("~", ""); //Remove ~
		//Don't add ~ for nicknames
		if (!nameWithLines.replace("|", "").replace(":", "").equalsIgnoreCase(name)) {
			player.sendMessage("§cThe name you gave doesn't match your name. Make sure to use "
                    + name + "§c with added vertical lines (|) or colons (:).");
			return true;
		}
		String[] nameparts = nameWithLines.split("[|:]");
		Color[] towncolors = TownColorComponent.TownColors.get(town.getName().toLowerCase());
		if (towncolors == null) {
			player.sendMessage("§cYour town doesn't have a color set. The town mayor can set it using /u towncolor.");
			return true;
		}
		if (nameparts.length < towncolors.length + 1) { //+1: Nation color
			player.sendMessage("§cYou need more vertical lines (|) or colons (:) in your name. (Should have " + (towncolors.length - 1 + 1) + ")"); //Nation color
			return true;
		}
		if (nameparts.length > (towncolors.length + 1) * 2) {
			player.sendMessage("§cYou have waay too many vertical lines (|) or colons (:) in your name. (Should have " + (towncolors.length - 1 + 1) + ")");
			return true;
		}
		if (nameparts.length > towncolors.length + 1) {
			player.sendMessage("§cYou have too many vertical lines (|) or colons (:) in your name. (Should have " + (towncolors.length - 1 + 1) + ")");
			return true;
		}
		ChatPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class).NameColorLocations()
			.set(new ArrayList<>(Arrays.stream(nameparts).map(String::length).collect(Collectors.toList()))); // No byte[], no TIntArrayList
		TownColorComponent.updatePlayerColors(player);
        player.sendMessage("§bName colors set: " + player.getDisplayName());
		return true;
	}
}
