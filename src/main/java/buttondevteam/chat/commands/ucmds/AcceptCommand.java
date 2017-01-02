package buttondevteam.chat.commands.ucmds;

import java.util.Timer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.FlairStates;
import buttondevteam.chat.PlayerJoinTimerTask;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.player.TBMCPlayer;

public class AcceptCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Accept flair ----", //
				"Accepts a flair from Reddit", //
				"Use /u accept <username> if you commented from multiple accounts" //
		};
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		final Player player = (Player) sender;
		ChatPlayer p = TBMCPlayer.getPlayerAs(player, ChatPlayer.class);
		if (args.length < 1 && p.getUserNames().size() > 1) {
			player.sendMessage("§9Multiple users commented your name. §bPlease pick one using /u accept <username>");
			StringBuilder sb = new StringBuilder();
			sb.append("§6Usernames:");
			for (String username : p.getUserNames())
				sb.append(" ").append(username);
			player.sendMessage(sb.toString());
			return true;
		}
		if (p.getFlairState().equals(FlairStates.NoComment) || p.getUserNames().size() == 0) {
			player.sendMessage("§cError: You need to write your username to the reddit thread at /r/ChromaGamers§r");
			return true;
		}
		if (args.length > 0 && !p.getUserNames().contains(args[0])) {
			player.sendMessage("§cError: Unknown name: " + args[0] + "§r");
			return true;
		}
		if (p.Working) {
			player.sendMessage("§cError: Something is already in progress.§r");
			return true;
		}

		if ((args.length > 0 ? args[0] : p.getUserNames().get(0)).equals(p.getUserName())) {
			player.sendMessage("§cYou already have this user's flair.§r");
			return true;
		}
		if (args.length > 0)
			p.setUserName(args[0]);
		else
			p.setUserName(p.getUserNames().get(0));

		player.sendMessage("§bObtaining flair...");
		p.Working = true;
		Timer timer = new Timer();
		PlayerJoinTimerTask tt = new PlayerJoinTimerTask() {
			@Override
			public void run() {
				try {
					PluginMain.Instance.DownloadFlair(mp);
				} catch (Exception e) {
					TBMCCoreAPI.SendException(
							"An error occured while downloading flair for " + player.getCustomName() + "!", e);
					player.sendMessage(
							"Sorry, but an error occured while trying to get your flair. Please contact a mod.");
					mp.Working = false;
					return;
				}

				if (mp.getFlairState().equals(FlairStates.Commented)) {
					player.sendMessage(
							"Sorry, but your flair isn't recorded. Please ask an admin to set it for you. Also, prepare a comment on /r/thebutton, if possible.");
					mp.Working = false;
					return;
				}
				String flair = mp.GetFormattedFlair();
				mp.setFlairState(FlairStates.Accepted);
				PluginMain.ConfirmUserMessage(mp);
				player.sendMessage("§bYour flair has been set:§r " + flair);
				mp.Working = false;
			}
		};
		tt.mp = p;
		timer.schedule(tt, 20);
		return true;
	}

	@Override
	public String GetUCommandPath() {
		return "accept";
	}

}
