package buttondevteam.thebuttonmcchat.commands.ucmds.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.thebuttonmcchat.FlairStates;
import buttondevteam.thebuttonmcchat.ChatPlayer;

public class SetFlairCommand extends AdminCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Set flair -----", "Set a flair for a player",
				"Usage: /u admin setflair <player> <flairtime (or non-presser, cant-press, none)> <cheater(true/false)> [username]",
				"Example 1: /u admin setflair NorbiPeti 19 false NorbiPeti --> orange (19s)",
				"Example 2: /u admin setflair iie 0 true asde --> purple (0s)" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length < 3) {
			return false;
		}
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			sender.sendMessage("§cPlayer not found.&r");
			return true;
		}
		short flairtime = 0x00;
		if (args[1].equalsIgnoreCase("non-presser"))
			flairtime = ChatPlayer.FlairTimeNonPresser;
		else if (args[1].equalsIgnoreCase("cant-press"))
			flairtime = ChatPlayer.FlairTimeCantPress;
		else if (args[1].equalsIgnoreCase("none"))
			flairtime = ChatPlayer.FlairTimeNone;
		else {
			try {
				flairtime = Short.parseShort(args[1]);
			} catch (Exception e) {
				sender.sendMessage(
						"§cFlairtime must be a number, \"non-presser\", \"cant-press\" or \"none\". Run without args to see usage.");
				return true;
			}
		} // TODO: Split config to per-player
		boolean cheater = false;
		if (args[2].equalsIgnoreCase("true"))
			cheater = true;
		else if (args[2].equalsIgnoreCase("false"))
			cheater = false;
		else {
			sender.sendMessage("§cUnknown value for cheater parameter. Run without args to see usage.");
			return true;
		}
		ChatPlayer mp = ChatPlayer.GetFromPlayer(p);
		mp.SetFlair(flairtime, cheater);
		mp.FlairState = FlairStates.Accepted;
		if (args.length < 4)
			mp.UserName = "";
		else {
			mp.UserName = args[3];
			if (!mp.UserNames.contains(args[3]))
				mp.UserNames.add(args[3]);
		}
		sender.sendMessage(
				"§bThe flair has been set. Player: " + mp.PlayerName + " Flair: " + mp.GetFormattedFlair() + "§r");
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "setflair";
	}

}
