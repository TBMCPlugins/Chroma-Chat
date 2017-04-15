package buttondevteam.chat.commands.ucmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.FlairStates;
import buttondevteam.lib.player.TBMCPlayer;

public final class IgnoreCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Ignore flair ----",
				"Stop the \"write your name in the thread\" message from showing up",
				"Use /u ignore <username> if you commented from multiple accounts" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		final Player player = (Player) sender;
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

	@Override
	public String GetUCommandPath() {
		return "ignore";
	}

}
