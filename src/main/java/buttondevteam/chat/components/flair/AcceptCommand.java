package buttondevteam.chat.components.flair;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PlayerJoinTimerTask;
import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.OptionallyPlayerCommandClass;
import buttondevteam.lib.player.TBMCPlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Timer;

@CommandClass(modOnly = false, helpText = {
	"Accept flair", //
	"Accepts a flair from Reddit", //
	"Use /u accept <username> if you commented from multiple accounts"
})
@OptionallyPlayerCommandClass(playerOnly = true)
@RequiredArgsConstructor
public class AcceptCommand extends UCommandBase {
	private final FlairComponent component;

	@Command2.Subcommand
	public boolean def(CommandSender sender, @Command2.OptionalArg String username) {
		final Player player = (Player) sender;
		ChatPlayer p = TBMCPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class);
		if (username == null && p.UserNames().size() > 1) {
			player.sendMessage("§9Multiple users commented your name. §bPlease pick one using /u accept <username>");
			StringBuilder sb = new StringBuilder();
			sb.append("§6Usernames:");
			for (String name : p.UserNames())
				sb.append(" ").append(name);
			player.sendMessage(sb.toString());
			return true;
		}
		if (p.FlairState().get().equals(FlairStates.NoComment) || p.UserNames().size() == 0) {
			player.sendMessage("§cError: You need to write your username to the reddit thread§r");
			player.sendMessage(component.FlairThreadURL().get());
			return true;
		}
		if (username != null && !p.UserNames().contains(username)) {
			player.sendMessage("§cError: Unknown name: " + username + "§r");
			return true;
		}
		if (p.Working) {
			player.sendMessage("§cError: Something is already in progress.§r");
			return true;
		}

		if ((username != null ? username : p.UserNames().get(0)).equals(p.UserName().get())) {
			player.sendMessage("§cYou already have this user's flair.§r");
			return true;
		}
		if (username != null)
			p.UserName().set(username);
		else
			p.UserName().set(p.UserNames().get(0));

		player.sendMessage("§bObtaining flair...");
		p.Working = true;
		Timer timer = new Timer();
		PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
			@Override
			public void run() {
				try {
					component.DownloadFlair(mp);
				} catch (Exception e) {
					TBMCCoreAPI.SendException(
							"An error occured while downloading flair for " + player.getCustomName() + "!", e);
					player.sendMessage(
							"Sorry, but an error occured while trying to get your flair. Please contact a mod.");
					mp.Working = false;
					return;
				}

				if (mp.FlairState().get().equals(FlairStates.Commented)) {
					player.sendMessage(
							"Sorry, but your flair isn't recorded. Please ask an admin to set it for you. Also, prepare a comment on /r/thebutton, if possible.");
					mp.Working = false;
					return;
				}
				String flair = mp.GetFormattedFlair();
				mp.FlairState().set(FlairStates.Accepted);
				FlairComponent.ConfirmUserMessage(mp);
				player.sendMessage("§bYour flair has been set:§r " + flair);
				mp.Working = false;
			}
		};
		tt.mp = p;
		timer.schedule(tt, 20);
		return true;
	}
}
