package buttondevteam.chat.components.fun;

import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import buttondevteam.lib.player.TBMCYEEHAWEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandClass(modOnly = false, helpText = {
	"YEEHAW command",
	"This command makes you YEEHAW."
})
public class YeehawCommand extends ICommand2MC {
	@Command2.Subcommand
	public boolean def(CommandSender sender) {
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
