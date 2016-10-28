package buttondevteam.chat.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.TBMCCommandBase;

public class YeehawCommand extends TBMCCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- YEEHAW command ----",
				"This command makes you YEEHAW." };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		for (Player p : PluginMain.GetPlayers()) {
			p.playSound(p.getLocation(), "tbmc.yeehaw", 1f, 1f);
			p.sendMessage("§b* "
					+ (sender instanceof Player ? ((Player) sender)
							.getDisplayName() : sender.getName()) + " §bYEEHAWs.");
		} //Even a cmdblock could yeehaw in theory
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
