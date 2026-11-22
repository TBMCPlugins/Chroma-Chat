package buttondevteam.chat.components.`fun`

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.PluginMain
import buttondevteam.lib.chat.Command2.OptionalArg
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.CustomTabCompleteMethod
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.player.TBMCPlayerBase.Companion.getPlayer
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.*

@CommandClass(
    path = "u c", helpText = ["Rainbow mode", "This command allows you to talk in rainbow colors"
    ]
)
class CCommand : ICommand2MC() {
    @Subcommand
    fun def(player: Player, @OptionalArg color: String?): Boolean {
        val p = getPlayer(player.uniqueId, ChatPlayer::class.java)
        if (color == null) {
            if (PluginMain.permission!!.has(player, "chroma.color.rainbow")) {
                p.RainbowPresserColorMode = !p.RainbowPresserColorMode
                p.OtherColorMode = null
                if (p.RainbowPresserColorMode) player.sendMessage("§eRainbow colors §aenabled.")
                else player.sendMessage("§eRainbow colors §cdisabled.")
            } else {
                player.sendMessage("§cYou don't have permission for this command.")
                return true
            }
        } else {
            if (PluginMain.permission!!.has(player, "chroma.color.custom")) {
                val x = color.lowercase(Locale.getDefault())
                if ("off" == x) {
                    p.OtherColorMode = null
                    player.sendMessage("§eMessage color reset.")
                    return true
                }
                val oc = NamedTextColor.NAMES.value(x)
                if (oc == null) {
                    player.sendMessage("§cUnknown message color: $color")
                    player.sendMessage("§cUse color names, like blue, or dark_aqua")
                    player.sendMessage("§cOr use 'off' to disable")
                    return true
                }
                p.RainbowPresserColorMode = false
                p.OtherColorMode = oc
                player.sendMessage(String.format("§eMessage color set to %s", p.OtherColorMode))
            } else {
                player.sendMessage("§cYou don't have permission for this command.")
                return true
            }
        }
        return true
    }

    @CustomTabCompleteMethod(param = "color")
    fun def(): Iterable<String> {
        return arrayOf("off", *NamedTextColor.NAMES.keys().toTypedArray()).asIterable()
    }
}
