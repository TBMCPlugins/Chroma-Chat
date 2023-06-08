package buttondevteam.chat.components.fun;

import buttondevteam.core.component.channel.Channel;
import buttondevteam.lib.TBMCChatEventBase;
import buttondevteam.lib.TBMCSystemChatEvent;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import buttondevteam.lib.chat.TBMCChatAPI;
import buttondevteam.lib.player.ChromaGamerBase;
import buttondevteam.lib.player.TBMCPlayerBase;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@CommandClass(modOnly = false, helpText = {
	"Unlol/unlaugh",
	"This command is based on an inside joke",
	"It will make the last person saying one of the recognized laugh strings blind for a few seconds",
	"Note that you can only unlaugh laughs that weren't unlaughed before"
})
@RequiredArgsConstructor
public final class UnlolCommand extends ICommand2MC {

	public Map<Channel, LastlolData> Lastlol = new HashMap<>();

	private final TBMCSystemChatEvent.BroadcastTarget target;

	@Command2.Subcommand
	public boolean def(ChromaGamerBase sender) {
		LastlolData lol = Lastlol.values().stream().filter(lld -> lld.Chatevent.shouldSendTo(sender))
			.max(Comparator.comparingLong(lld -> lld.Loltime)).orElse(null);
		if (lol == null)
			return true;
		if (lol.Lolowner instanceof TBMCPlayerBase) {
			var player = ((TBMCPlayerBase) lol.Lolowner).getPlayer();
			if (player != null)
				player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 5, false, false));
		}
		String msg = sender.getName() + (lol.Lolornot ? " unlolled " : " unlaughed ") + lol.Lolowner.getName();
		TBMCChatAPI.SendSystemMessage(Channel.globalChat, Channel.RecipientTestResult.ALL, msg, target);
		Lastlol.remove(lol.Chatevent.getChannel());
		return true;
	}

	@Data
	public static class LastlolData {
		private boolean Lolornot;
		private final ChromaGamerBase Lolowner;
		private final TBMCChatEventBase Chatevent;
		private final long Loltime;
	}
}
