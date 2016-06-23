package io.github.norbipeti.thebuttonmcchat.commands;

import io.github.norbipeti.thebuttonmcchat.PluginMain;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
							.getDisplayName() : sender.getName()) + " YEEHAWs.");
		} //Even a cmdblock could yeehaw in theory
		return true;
	}

	@Override
	public String GetCommandName() {
		return "yeehaw";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

}
