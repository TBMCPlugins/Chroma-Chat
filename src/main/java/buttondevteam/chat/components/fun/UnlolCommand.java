package buttondevteam.chat.components.fun;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCChatEventBase;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.ThorpeUtils;
import buttondevteam.lib.chat.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@CommandClass(modOnly = false, helpText = {
	"§6---- Unlol/unlaugh ----",
	"This command is based on a joke between NorbiPeti and Ghostise",
	"It will make the last person saying one of the recognized laugh strings blind for a few seconds",
	"Note that you can only unlaugh laughs that weren't unlaughed before"
})
@RequiredArgsConstructor
public final class UnlolCommand extends ICommand2MC {

	public Map<Channel, LastlolData> Lastlol = new HashMap<>();

	private final TBMCSystemChatEvent.BroadcastTarget target;

	@Command2.Subcommand
	public boolean def(CommandSender sender) {
		LastlolData lol = Lastlol.values().stream().filter(lld -> lld.Chatevent.shouldSendTo(sender))
				.max(Comparator.comparingLong(lld -> lld.Loltime)).orElse(null);
		if (lol == null)
			return true;
		if (lol.Lolowner instanceof Player)
			((Player) lol.Lolowner)
					.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 5, false, false));
		String msg = ThorpeUtils.getDisplayName(sender)
				+ (lol.Lolornot ? " unlolled " : " unlaughed ")
				+ ThorpeUtils.getDisplayName(lol.Lolowner);
		TBMCChatAPI.SendSystemMessage(Channel.GlobalChat, Channel.RecipientTestResult.ALL, msg, target);
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
