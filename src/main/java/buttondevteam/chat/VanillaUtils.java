package buttondevteam.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCChatEvent;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.server.v1_12_R1.ChatComponentUtils;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
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
				if (notCraftPlayer(cl)) return true; // p instanceof CraftPlayer
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

	private BiConsumer<Player, String> tellRaw;

	public boolean tellRaw(Player p, String json) {
		try {
			val pcl = p.getClass();
			if (notCraftPlayer(pcl)) return false;
			val hm = pcl.getMethod("getHandle");
			val handle = hm.invoke(p); ;
			val nms = handle.getClass().getPackage().getName();
			val chatcompcl = Class.forName(nms + ".IChatBaseComponent");
			val sendmsg = handle.getClass().getMethod("sendMessage", chatcompcl);

			val ccucl = Class.forName(nms + ".ChatComponentUtils");
			val iclcl = Class.forName(nms + ".ICommandListener");
			val encl = Class.forName(nms + ".Entity");
			val ffdm = ccucl.getMethod("filterForDisplay", iclcl, chatcompcl, encl);

			val cscl = Class.forName(chatcompcl.getName() + "$ChatSerializer");
			val am = cscl.getMethod("a", String.class);
			val deserialized = am.invoke(null, json);
			val filtered = ffdm.invoke(null, handle, deserialized, handle); //TODO: Use BiConsumer
			sendmsg.invoke(handle, filtered);

			((CraftPlayer) p).getHandle().sendMessage(ChatComponentUtils
				.filterForDisplay(((CraftPlayer) p).getHandle(),
					IChatBaseComponent.ChatSerializer.a(json), ((CraftPlayer) p).getHandle()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean notCraftPlayer(Class<?> cl) {
		return !cl.getSimpleName().contains("CraftPlayer");
	}
}
