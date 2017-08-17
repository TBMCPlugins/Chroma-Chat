package buttondevteam.chat;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import buttondevteam.lib.TBMCChatEvent;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.EntityHuman.EnumChatVisibility;

@UtilityClass
public class VanillaUtils {
	public int getMCScoreIfChatOn(Player p, TBMCChatEvent e) {
		if (!(p instanceof CraftPlayer) || ((CraftPlayer) p).getHandle().getChatFlags() == EnumChatVisibility.FULL) // Only send if client allows chat
			return e.getMCScore(p);
		else
			return -1;
	}
}
