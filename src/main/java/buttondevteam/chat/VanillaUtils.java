package buttondevteam.chat;

import buttondevteam.lib.TBMCChatEvent;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.EntityHuman.EnumChatVisibility;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

@UtilityClass
public class VanillaUtils {
	public int getMCScoreIfChatOn(Player p, TBMCChatEvent e) {
		if (!(p instanceof CraftPlayer) || ((CraftPlayer) p).getHandle().getChatFlags() == EnumChatVisibility.FULL) // Only send if client allows chat
			return e.getMCScore(p);
		else
			return -1;
	}

	/*private String version;

	public short getMCVersion() {
		if (version != null) return version;
		val v = ChatUtils.coolSubstring(Bukkit.getServer().getVersion().getClass().getPackage().getName(),
			"org.bukkit.craftbukkit.v", "_R1").orElse("1_8").replace("_", "");
		return Short.parseShort(v);
	}*/
}
