package buttondevteam.chat.components.towncolors.admin

import buttondevteam.chat.PluginMain
import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase
import buttondevteam.chat.components.towncolors.TownColorComponent
import buttondevteam.chat.components.towncolors.TownyListener
import buttondevteam.chat.components.towny.TownyComponent
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.CustomTabCompleteMethod
import com.palmergames.bukkit.towny.`object`.Nation
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import java.util.*

@CommandClass(
    helpText = [
        "Nation color",
        "Sets the color of the nation."
    ]
)
class NationColorCommand : AdminCommandBase() {
    @Command2.Subcommand
    fun def(sender: org.bukkit.command.CommandSender, nation: kotlin.String, color: kotlin.String): kotlin.Boolean {
        val n = TownyComponent.dataSource!!.getNation(nation)
        if (n == null) {
            sender.sendMessage("§cThe nation '$nation' cannot be found.")
            return true
        }
        return SetNationColor(sender, n, color)
    }

    @CustomTabCompleteMethod(param = "nation")
    fun def(): Iterable<String> {
        return Iterable {
            TownyComponent.dataSource!!.nations.map { it.name }.iterator()
        }
    }

    companion object {
        fun SetNationColor(sender: org.bukkit.command.CommandSender, nation: Nation, color: String): Boolean {
            val c = TownColorCommand.getColorOrSendError(color, sender) ?: return true
            if (c != NamedTextColor.WHITE) { //Default nation color
                for ((key, value) in TownColorComponent.NationColor) {
                    if (value == c) {
                        sender.sendMessage("§cThe nation $key already uses this color!")
                        return true
                    }
                }
            }
            TownColorComponent.NationColor[nation.name.lowercase(Locale.getDefault())] = c
            Bukkit.getScheduler().runTaskAsynchronously(PluginMain.Instance!!, Runnable {
                for (t in nation.towns) TownyListener.updateTownMembers(t)
            })
            sender.sendMessage("§bNation color set to " + TownColorCommand.getColorText(c))
            return true
        }
    }
}
