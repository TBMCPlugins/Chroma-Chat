package buttondevteam.chat.commands.ucmds.admin;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.Color;
import lombok.val;

public class TownColorCommand extends AdminCommandBase {
	@Override
	public String GetHelpText(String alias)[] { // TODO: Command path aliases
		return new String[] { //
				"§6---- Town Color ----", //
				"This command allows setting a color for a town.", //
				"The town will be shown with this color on Dynmap and all players in the town will appear in chat with these colors.", //
				"The colors will split the name evenly.", //
				"Usage: /" + GetCommandPath() + " <town> <colorname1> [colorname2...]", //
				"Example: /" + GetCommandPath() + " Alderon blue gray" //
		};
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length < 2)
			return false;
		if (!PluginMain.TU.getTownsMap().containsKey(args[0])) {
			sender.sendMessage("§cThe town '" + args[0] + "' cannot be found.");
			return true;
		}
		val clrs = new Color[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			val ii = i;
			val c = Arrays.stream(Color.values()).filter(cc -> cc.getName().equalsIgnoreCase(args[ii])).findAny();
			if (!c.isPresent()) {
				sender.sendMessage("§cThe color '" + args[i] + "' cannot be found.");
				return true;
			}
			clrs[i - 1] = c.get();
		}
		PluginMain.TownColors.put(args[0], clrs);
		sender.sendMessage("§bColor(s) set.");
		return true;
	}
}
