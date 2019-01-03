package buttondevteam.chat.commands.ucmds;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.chat.listener.PlayerJoinLeaveListener;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.OptionallyPlayerCommandClass;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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
		final String name = ChatColor.stripColor(player.getDisplayName()).replace("~", ""); //Remove ~
		String arg = args[0]; //Don't add ~ for nicknames
        if (!arg.replace("|", "").replace(":", "").equalsIgnoreCase(name)) {
			player.sendMessage("§cThe name you gave doesn't match your name. Make sure to use "
                    + name + "§c with added vertical lines (|) or colons (:).");
			return true;
		}
		String[] nameparts = arg.split("[|:]");
		Color[] towncolors = PluginMain.TownColors.get(town.getName().toLowerCase());
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
				.set(new ArrayList<>(Arrays.stream(nameparts).map(np -> np.length()).collect(Collectors.toList()))); // No byte[], no TIntArrayList
        PlayerJoinLeaveListener.updatePlayerColors(player);
        player.sendMessage("§bName colors set: " + player.getDisplayName());
		return true;
	}
}
