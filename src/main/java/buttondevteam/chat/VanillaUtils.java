package buttondevteam.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCChatEvent;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.server.v1_12_R1.ChatComponentUtils;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

@UtilityClass
public class VanillaUtils {
	public int getMCScoreIfChatOn(Player p, TBMCChatEvent e) {
		try {
			if (isChatOn(p)) // Only send if client allows chat
				return e.getMCScore(p);
			else
				return -1;
		} catch (NoClassDefFoundError ex) {
			MainPlugin.Instance.getLogger().warning("Compatibility error, can't check if the chat is hidden by the player.");
			return e.getMCScore(p);
		}
	}

	private Predicate<Player> isChatOn;

	private boolean isChatOn(Player p) {
		try {
			if (isChatOn == null) {
				val cl = p.getClass();
				if (!cl.getSimpleName().contains("CraftPlayer")) return true; // p instanceof CraftPlayer
				val hm = cl.getMethod("getHandle");
				val handle = hm.invoke(p); //p.getHandle()
				val vpcl = handle.getClass();
				val gcfm = vpcl.getMethod("getChatFlags");
				Class<?> encl;
				try {
					encl = Class.forName(handle.getClass().getPackage().getName() + ".EnumChatVisibility");
				} catch (ClassNotFoundException e) {
					encl = Class.forName(handle.getClass().getPackage().getName() + ".EntityHuman$EnumChatVisibility");
				}
				val ff = encl.getField("FULL");
				val full = ff.get(null); // EnumChatVisibility.FULL
				isChatOn = pl -> {
					try {
						val ph = hm.invoke(pl); //pl.getHandle()
						val flags = gcfm.invoke(ph); //handle.getChatFlags()
						return flags == full; //TODO: It's only checked if not global
					} catch (Exception e) {
						e.printStackTrace();
						return true;
					}
				};
			}
			return isChatOn.test(p);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	/*private String version;

	public short getMCVersion() {
		if (version != null) return version;
		val v = ChatUtils.coolSubstring(Bukkit.getServer().getVersion().getClass().getPackage().getName(),
			"org.bukkit.craftbukkit.v", "_R1").orElse("1_8").replace("_", "");
		return Short.parseShort(v);
	}*/

	public String tellRaw(Player p, String json) {
		try {
			ChatComponentUtils.filterForDisplay(((CraftPlayer) p).getHandle(), //TODO: Reflection
				IChatBaseComponent.ChatSerializer.a(json), ((CraftPlayer) p).getHandle());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
