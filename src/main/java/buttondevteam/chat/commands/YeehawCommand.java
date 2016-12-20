package buttondevteam.chat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.lib.TBMCYEEHAWEvent;
import buttondevteam.lib.chat.TBMCCommandBase;

public class YeehawCommand extends TBMCCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- YEEHAW command ----", "This command makes you YEEHAW." };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		final String message = "§b* "
				+ (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + " §bYEEHAWs.";
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.playSound(p.getLocation(), "tbmc.yeehaw", 1f, 1f);
		} // Even a cmdblock could yeehaw in theory
			// Or anyone from Discord
		Bukkit.broadcastMessage(message);
		Bukkit.getPluginManager().callEvent(new TBMCYEEHAWEvent(sender));
		return true;
	}

	@Override
	public String GetCommandPath() {
		return "yeehaw";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

	@Override
	public boolean GetModOnly() {
		return false;
	}
}
