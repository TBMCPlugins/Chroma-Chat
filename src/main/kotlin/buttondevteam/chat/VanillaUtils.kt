package buttondevteam.chat

import buttondevteam.core.MainPlugin
import buttondevteam.lib.TBMCChatEvent
import buttondevteam.lib.player.ChromaGamerBase
import com.destroystokyo.paper.ClientOption
import org.bukkit.entity.Player

object VanillaUtils {
    fun getGroupIfChatOn(p: Player, e: TBMCChatEvent): kotlin.String? {
        try {
            if (!isChatOn(p))  // Only send if client allows chat
                return null
        } catch (_: NoClassDefFoundError) {
            MainPlugin.instance.logger.warning("Compatibility error, can't check if the chat is hidden by the player.")
        }
        return e.getGroupID(ChromaGamerBase.getFromSender(p))
    }

    private fun isChatOn(p: Player): Boolean {
        return p.getClientOption(ClientOption.CHAT_VISIBILITY) == ClientOption.ChatVisibility.FULL
    }
}
