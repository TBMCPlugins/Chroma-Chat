package buttondevteam.chat

import lombok.experimental.UtilityClass

@UtilityClass
class VanillaUtils {
    fun getGroupIfChatOn(p: org.bukkit.entity.Player, e: TBMCChatEvent): kotlin.String? {
        try {
            if (!isChatOn(p))  // Only send if client allows chat
                return null
        } catch (ex: java.lang.NoClassDefFoundError) {
            MainPlugin.Companion.instance.getLogger()
                .warning("Compatibility error, can't check if the chat is hidden by the player.")
        }
        return e.getGroupID(ChromaGamerBase.Companion.getFromSender(p))
    }

    private var isChatOn: java.util.function.Predicate<org.bukkit.entity.Player?>? = null

    private fun isChatOn(p: org.bukkit.entity.Player): kotlin.Boolean {
        try {
            if (isChatOn == null) {
                val cl = p.javaClass
                if (notCraftPlayer(cl)) return true // p instanceof CraftPlayer

                val hm = cl.getMethod("getHandle")
                val handle = hm.invoke(p) //p.getHandle()
                val vpcl = handle.getClass()
                val gcfm = vpcl.getMethod("getChatFlags")
                var encl: java.lang.Class<*>
                try {
                    encl = java.lang.Class.forName(handle.getClass().getPackage().getName() + ".EnumChatVisibility")
                } catch (e: java.lang.ClassNotFoundException) {
                    encl = java.lang.Class.forName(
                        handle.getClass().getPackage().getName() + ".EntityHuman\$EnumChatVisibility"
                    )
                }
                val ff = encl.getField("FULL")
                val full = ff.get(null) // EnumChatVisibility.FULL
                isChatOn = java.util.function.Predicate { pl: org.bukkit.entity.Player? ->
                    try {
                        if (notCraftPlayer(pl!!.javaClass)) return@Predicate true //Need to check each time

                        val ph = hm.invoke(pl) //pl.getHandle()
                        val flags = gcfm.invoke(ph) //handle.getChatFlags()
                        return@Predicate flags === full
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        return@Predicate true
                    }
                }
            }
            return isChatOn!!.test(p)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return true
        }
    }

    /*private String version;

	public short getMCVersion() {
		if (version != null) return version;
		val v = ChatUtils.coolSubstring(Bukkit.getServer().getVersion().getClass().getPackage().getName(),
			"org.bukkit.craftbukkit.v", "_R1").orElse("1_8").replace("_", "");
		return Short.parseShort(v);
	}*/
    private var tellRaw: java.util.function.BiPredicate<org.bukkit.entity.Player?, kotlin.String?>? = null

    fun tellRaw(p: org.bukkit.entity.Player, json: kotlin.String?): kotlin.Boolean {
        try {
            if (tellRaw == null) {
                val pcl = p.javaClass
                if (notCraftPlayer(pcl)) return false
                val hm = pcl.getMethod("getHandle")
                val handle = hm.invoke(p)
                var nmsOrChat: Unit /* TODO: class org.jetbrains.kotlin.j2k.types.JKJavaNullPrimitiveType */? =
                    handle.getClass().getPackage().getName()
                if (!nmsOrChat.contains(".v1_")) nmsOrChat = "net.minecraft.network.chat"
                val chatcompcl = java.lang.Class.forName(nmsOrChat.toString() + ".IChatBaseComponent")
                //val chatcomarrcl = Class.forName("[L" + chatcompcl.getName() + ";");
                val chatcomparr = java.lang.reflect.Array.newInstance(chatcompcl, 1)
                val sendmsg: java.lang.reflect.Method
                run {
                    var sendmsg1: java.lang.reflect.Method
                    try {
                        sendmsg1 = handle.getClass()
                            .getMethod("sendMessage", java.util.UUID::class.java, chatcomparr.getClass())
                    } catch (e: java.lang.NoSuchMethodException) {
                        sendmsg1 = handle.getClass().getMethod("sendMessage", chatcomparr.getClass())
                    }
                    sendmsg = sendmsg1
                }

                /*val ccucl = Class.forName(nms + ".ChatComponentUtils");
				val iclcl = Class.forName(nms + ".ICommandListener");
				val encl = Class.forName(nms + ".Entity");
				val ffdm = ccucl.getMethod("filterForDisplay", iclcl, chatcompcl, encl);*/
                val cscl = java.lang.Class.forName(chatcompcl.getName() + "\$ChatSerializer")
                val am = cscl.getMethod("a", kotlin.String::class.java)

                tellRaw = java.util.function.BiPredicate { pl: org.bukkit.entity.Player?, jsonStr: kotlin.String? ->
                    if (notCraftPlayer(pl!!.javaClass)) return@BiPredicate false
                    try {
                        val hhandle = hm.invoke(pl)
                        val deserialized = am.invoke(null, jsonStr)
                        //val filtered = ffdm.invoke(null, hhandle, deserialized, hhandle);
                        java.lang.reflect.Array.set(chatcomparr, 0, deserialized)
                        if (sendmsg.getParameterCount() == 2) sendmsg.invoke(hhandle, null, chatcomparr) //
                        else sendmsg.invoke(hhandle, chatcomparr)
                        return@BiPredicate true
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        return@BiPredicate false
                    }
                }
            }

            /*((CraftPlayer) p).getHandle().sendMessage(ChatComponentUtils
				.filterForDisplay(((CraftPlayer) p).getHandle(),
					IChatBaseComponent.ChatSerializer.a(json), ((CraftPlayer) p).getHandle()));*/
            return tellRaw!!.test(p, json)
        } catch (e: java.lang.Exception) {
            PluginMain.Companion.Instance.getLogger().warning("Could not use tellRaw: " + e.message)
            return false
        }
    }

    private fun notCraftPlayer(cl: java.lang.Class<*>): kotlin.Boolean {
        return !cl.getSimpleName().contains("CraftPlayer")
    }
}
