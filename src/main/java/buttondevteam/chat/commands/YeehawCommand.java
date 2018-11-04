package buttondevteam.chat.commands;

import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import buttondevteam.lib.player.TBMCYEEHAWEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandClass(modOnly = false)
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
			p.sendMessage(message); //Not broadcasting, so the Discord plugin can handle the event in a special way
		} // Even a cmdblock could yeehaw in theory
			// Or anyone from Discord
		Bukkit.getPluginManager().callEvent(new TBMCYEEHAWEvent(sender));
		return true;
	}
}
