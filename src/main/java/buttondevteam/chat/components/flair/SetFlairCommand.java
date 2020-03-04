package buttondevteam.chat.components.flair;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.player.TBMCPlayerBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandClass(helpText = {
	"§6---- Set flair -----", "Set a flair for a player",
	"Usage: /u admin setflair <player> <flairtime (or non-presser, cant-press, none)> <cheater(true/false)> [username]",
	"Example 1: /u admin setflair NorbiPeti 19 false NorbiPeti --> orange (19s)",
	"Example 2: /u admin setflair iie 0 true asde --> purple (0s)"
})
public class SetFlairCommand extends AdminCommandBase {
	@Command2.Subcommand
	public boolean def(CommandSender sender, String player, String flairtime, boolean cheater, @Command2.OptionalArg String username) {
		Player p = Bukkit.getPlayer(player);
		if (p == null) {
			sender.sendMessage("§cPlayer not found.&r");
			return true;
		}
		short ft;
		if (flairtime.equalsIgnoreCase("non-presser"))
			ft = ChatPlayer.FlairTimeNonPresser;
		else if (flairtime.equalsIgnoreCase("cant-press"))
			ft = ChatPlayer.FlairTimeCantPress;
		else if (flairtime.equalsIgnoreCase("none"))
			ft = ChatPlayer.FlairTimeNone;
		else {
			try {
				ft = Short.parseShort(flairtime);
			} catch (Exception e) {
				sender.sendMessage(
						"§cFlairtime must be a number, \"non-presser\", \"cant-press\" or \"none\". Run without args to see usage.");
				return true;
			}
		}
		ChatPlayer mp = TBMCPlayerBase.getPlayer(p.getUniqueId(), ChatPlayer.class);
		mp.SetFlair(ft, cheater);
		mp.FlairState().set(FlairStates.Accepted);
		if (username == null)
			mp.UserName().set("");
		else {
			mp.UserName().set(username);
			if (!mp.UserNames().contains(username))
				mp.UserNames().add(username);
		}
		sender.sendMessage(
				"§bThe flair has been set. Player: " + mp.PlayerName() + " Flair: " + mp.GetFormattedFlair() + "§r");
		return true;
	}

}
