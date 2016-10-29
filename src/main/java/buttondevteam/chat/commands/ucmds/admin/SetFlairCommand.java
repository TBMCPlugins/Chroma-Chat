package buttondevteam.chat.commands.ucmds.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.FlairStates;
import buttondevteam.lib.TBMCPlayer;

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
		}
		boolean cheater = false;
		if (args[2].equalsIgnoreCase("true"))
			cheater = true;
		else if (args[2].equalsIgnoreCase("false"))
			cheater = false;
		else {
			sender.sendMessage("§cUnknown value for cheater parameter. Run without args to see usage.");
			return true;
		}
		ChatPlayer mp = TBMCPlayer.getPlayerAs(p, ChatPlayer.class);
		mp.SetFlair(flairtime, cheater);
		mp.setFlairState(FlairStates.Accepted);
		if (args.length < 4)
			mp.setUserName("");
		else {
			mp.setUserName(args[3]);
			if (!mp.getUserNames().contains(args[3]))
				mp.getUserNames().add(args[3]);
		}
		sender.sendMessage(
				"§bThe flair has been set. Player: " + mp.getPlayerName() + " Flair: " + mp.GetFormattedFlair() + "§r");
		return true;
	}

	@Override
	public String GetAdminCommandPath() {
		return "setflair";
	}

}
