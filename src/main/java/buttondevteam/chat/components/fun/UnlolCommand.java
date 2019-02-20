package buttondevteam.chat.components.fun;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCChatEventBase;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.TBMCCommandBase;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@CommandClass(modOnly = false)
public final class UnlolCommand extends TBMCCommandBase {

	public static Map<Channel, LastlolData> Lastlol = new HashMap<>();

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Unlol/unlaugh ----",
				"This command is based on a joke between NorbiPeti and Ghostise",
				"It will make the last person saying one of the recognized laugh strings blind for a few seconds",
				"Note that you can only unlaugh laughs that weren't unlaughed before" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		LastlolData lol = Lastlol.values().stream().filter(lld -> lld.Chatevent.shouldSendTo(sender))
				.max(Comparator.comparingLong(lld -> lld.Loltime)).orElse(null);
		if (lol == null)
			return true;
		if (lol.Lolowner instanceof Player)
			((Player) lol.Lolowner)
					.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 5, false, false));
		String msg = (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName())
				+ (lol.Lolornot ? " unlolled " : " unlaughed ")
				+ (lol.Lolowner instanceof Player ? ((Player) lol.Lolowner).getDisplayName() : lol.Lolowner.getName());
		Bukkit.broadcastMessage(msg);
		Lastlol.remove(lol.Chatevent.getChannel());
		return true;
	}

	@Data
	public static class LastlolData {
		private boolean Lolornot;
		private final CommandSender Lolowner;
		private final TBMCChatEventBase Chatevent;
		private final long Loltime;
	}
}
