package io.github.norbipeti.thebuttonmcchat.commands;

import io.github.norbipeti.thebuttonmcchat.PluginMain;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class UnlolCommand extends TBMCCommandBase {

	public static CommandSender Lastlol = null;
	public static boolean Lastlolornot;

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] {
				"§6---- Unlol/unlaugh ----",
				"This command is based on a joke between NorbiPeti and Ghostise",
				"It will make anyone saying one of the recognized laugh strings blind for a few seconds",
				"Note that you can only unlaugh laughs that weren't unlaughed before" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (Lastlol != null) {
			if (Lastlol instanceof Player)
				((Player) Lastlol).addPotionEffect(new PotionEffect(
						PotionEffectType.BLINDNESS, 10 * 20, 5, false, false));
			String msg = (sender instanceof Player ? ((Player) sender)
					.getDisplayName() : sender.getName())
					+ (Lastlolornot ? " unlolled " : " unlaughed ")
					+ (Lastlol instanceof Player ? ((Player) Lastlol)
							.getDisplayName() : Lastlol.getName());
			for (Player pl : PluginMain.GetPlayers())
				pl.sendMessage(msg);
			Bukkit.getServer().getConsoleSender().sendMessage(msg);
			Lastlol = null;
		}
		return true;
	}

	@Override
	public String GetCommandPath() {
		return "unlol";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

}
