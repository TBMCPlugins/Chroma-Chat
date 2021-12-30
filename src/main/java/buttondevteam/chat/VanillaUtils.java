package buttondevteam.chat;

import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCChatEvent;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@UtilityClass
public class VanillaUtils {
	public String getGroupIfChatOn(Player p, TBMCChatEvent e) {
		try {
			if (isChatOn(p)) // Only send if client allows chat
				return e.getGroupID(p);
			else
				return null;
		} catch (NoClassDefFoundError ex) {
			MainPlugin.Instance.getLogger().warning("Compatibility error, can't check if the chat is hidden by the player.");
			return e.getGroupID(p);
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
						if (notCraftPlayer(pl.getClass())) return true; //Need to check each time
						val ph = hm.invoke(pl); //pl.getHandle()
						val flags = gcfm.invoke(ph); //handle.getChatFlags()
						return flags == full;
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

	private BiPredicate<Player, String> tellRaw;

	public boolean tellRaw(Player p, String json) {
		try {
			if (tellRaw == null) {
				val pcl = p.getClass();
				if (notCraftPlayer(pcl)) return false;
				val hm = pcl.getMethod("getHandle");
				val handle = hm.invoke(p);
				var nmsOrChat = handle.getClass().getPackage().getName();
				if (!nmsOrChat.contains(".v1_"))
					nmsOrChat = "net.minecraft.network.chat";
				val chatcompcl = Class.forName(nmsOrChat + ".IChatBaseComponent");
				//val chatcomarrcl = Class.forName("[L" + chatcompcl.getName() + ";");
				val chatcomparr = Array.newInstance(chatcompcl, 1);
				final Method sendmsg;
				{
					Method sendmsg1;
					try {
						sendmsg1 = handle.getClass().getMethod("sendMessage", UUID.class, chatcomparr.getClass());
					} catch (NoSuchMethodException e) {
						sendmsg1 = handle.getClass().getMethod("sendMessage", chatcomparr.getClass());
					}
					sendmsg = sendmsg1;
				}

				/*val ccucl = Class.forName(nms + ".ChatComponentUtils");
				val iclcl = Class.forName(nms + ".ICommandListener");
				val encl = Class.forName(nms + ".Entity");
				val ffdm = ccucl.getMethod("filterForDisplay", iclcl, chatcompcl, encl);*/

				val cscl = Class.forName(chatcompcl.getName() + "$ChatSerializer");
				val am = cscl.getMethod("a", String.class);

				tellRaw = (pl, jsonStr) -> {
					if (notCraftPlayer(pl.getClass())) return false;
					try {
						val hhandle = hm.invoke(pl);
						val deserialized = am.invoke(null, jsonStr);
						//val filtered = ffdm.invoke(null, hhandle, deserialized, hhandle);
						Array.set(chatcomparr, 0, deserialized);
						if (sendmsg.getParameterCount() == 2)
							sendmsg.invoke(hhandle, null, chatcomparr); //
						else
							sendmsg.invoke(hhandle, chatcomparr);
						return true;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				};
			}

			/*((CraftPlayer) p).getHandle().sendMessage(ChatComponentUtils
				.filterForDisplay(((CraftPlayer) p).getHandle(),
					IChatBaseComponent.ChatSerializer.a(json), ((CraftPlayer) p).getHandle()));*/
			return tellRaw.test(p, json);
		} catch (Exception e) {
			PluginMain.Instance.getLogger().warning("Could not use tellRaw: " + e.getMessage());
			return false;
		}
	}

	private boolean notCraftPlayer(Class<?> cl) {
		return !cl.getSimpleName().contains("CraftPlayer");
	}
}
