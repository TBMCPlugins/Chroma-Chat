package buttondevteam.chat.commands.ucmds.admin;

import buttondevteam.chat.PluginMain;
import buttondevteam.chat.listener.TownyListener;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class NationColorCommand extends AdminCommandBase {
	@Override
	public String[] GetHelpText(String alias) {
		return new String[]{ //
				"§6---- Nation color ----", //
				"Sets the color of the nation.", //
				"Usage: /u admin nationcolor <color>" //
		};
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		return SetNationColor(sender, alias, args);
	}

	public static boolean SetNationColor(CommandSender sender, String alias, String[] args) {
		if (args.length < 2)
			return false;
		if (args.length > 2) {
			sender.sendMessage("§cYou can only use one color as a nation color.");
			return true;
		}
		final Nation nation = PluginMain.TU.getNationsMap().get(args[0].toLowerCase());
		if (nation == null) {
			sender.sendMessage("§cThe nation '" + args[0] + "' cannot be found.");
			return true;
		}
		val c = TownColorCommand.getColorOrSendError(args[1], sender);
		if (!c.isPresent()) return true;
		PluginMain.NationColor.put(args[0].toLowerCase(), c.get());
		Bukkit.getScheduler().runTaskAsynchronously(PluginMain.Instance, () -> {
			for (Town t : nation.getTowns())
				TownyListener.updateTownMembers(t);
		});
		sender.sendMessage("§bNation color set to §" + TownColorCommand.getColorText(c.get()));
		return true;
	}
}
