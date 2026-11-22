package buttondevteam.chat.components.`fun`

import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.TBMCChatEventBase
import buttondevteam.lib.TBMCSystemChatEvent
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.chat.TBMCChatAPI
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayerBase

@CommandClass(
    modOnly = false,
    helpText = [
        "Unlol/unlaugh",
        "This command is based on an inside joke",
        "It will make the last person saying one of the recognized laugh strings blind for a few seconds",
        "Note that you can only unlaugh laughs that weren't unlaughed before"
    ]
)
class UnlolCommand(private val target: TBMCSystemChatEvent.BroadcastTarget) : ICommand2MC() {
    var Lastlol = mutableMapOf<Channel, LastlolData>()

    @Command2.Subcommand
    fun def(sender: ChromaGamerBase): Boolean {
        val lol = Lastlol.values.filter { lld -> lld.Chatevent.shouldSendTo(sender) }
            .maxByOrNull { it.Loltime }
        if (lol == null) return true
        if (lol.Lolowner is TBMCPlayerBase) {
            lol.Lolowner.player?.addPotionEffect(
                org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.BLINDNESS,
                    2 * 20,
                    5,
                    false,
                    false
                )
            )
        }
        val msg = sender.name + (if (lol.Lolornot) " unlolled " else " unlaughed ") + lol.Lolowner.name
        TBMCChatAPI.SendSystemMessage(Channel.globalChat, Channel.RecipientTestResult.ALL, msg, target)
        Lastlol.remove(lol.Chatevent.channel)
        return true
    }

    class LastlolData(val Lolowner: ChromaGamerBase, val Chatevent: TBMCChatEventBase, val Loltime: Long) {
        var Lolornot = false
    }
}
