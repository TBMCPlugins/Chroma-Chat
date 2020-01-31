package buttondevteam.chat.components.flair;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.player.TBMCPlayer;
import org.bukkit.entity.Player;

@CommandClass(modOnly = false, helpText = {
	"Ignore flair",
	"Stop the \"write your name in the thread\" message from showing up"
})
public final class IgnoreCommand extends UCommandBase {
	@Command2.Subcommand
	public boolean def(Player player) {
		ChatPlayer p = TBMCPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class);
		if (p.FlairState().get().equals(FlairStates.Accepted)) {
			player.sendMessage("§cYou can only ignore the \"write your name in the thread\" message.");
			return true;
		}
		if (p.FlairState().get().equals(FlairStates.Commented)) {
			player.sendMessage("Sorry, but your flair isn't recorded. Please ask a mod to set it for you.");
			return true;
		}
		if (!p.FlairState().get().equals(FlairStates.Ignored)) {
			p.FlairState().set(FlairStates.Ignored);
			p.SetFlair(ChatPlayer.FlairTimeNone);
			p.UserName().set("");
			player.sendMessage("§bYou have ignored the message.§r");
		} else
			player.sendMessage("§cYou already ignored the message.§r");
		return true;
	}
}
