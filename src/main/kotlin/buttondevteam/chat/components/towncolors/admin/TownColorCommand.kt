package buttondevteam.chat.components.towncolors.admin

import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase
import buttondevteam.chat.components.towncolors.TownColorComponent
import buttondevteam.chat.components.towncolors.TownyListener
import buttondevteam.chat.components.towny.TownyComponent
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.CustomTabCompleteMethod
import com.palmergames.bukkit.towny.`object`.Town
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import java.util.*

@CommandClass(
    helpText = [
        "Town Color",
        "This command allows setting a color for a town.",
        "The town will be shown with this color on Dynmap and all players in the town will appear in chat with these colors.",
        "The colors will split the name evenly."
    ]
)
class TownColorCommand : AdminCommandBase() {
    @Command2.Subcommand
    fun def(sender: CommandSender, town: String, vararg colornames: String): Boolean {
        if (TownyComponent.dataSource!!.getTown(town) == null) {
            sender.sendMessage("§cThe town '$town' cannot be found.")
            return true
        }
        val targetTown = TownyComponent.dataSource!!.getTown(town)
        if (targetTown == null) {
            sender.sendMessage("§cThe town '$town' cannot be found.")
            return true
        }
        return SetTownColor(sender, targetTown, colornames)
    }

    @CustomTabCompleteMethod(param = "town")
    fun def(): Iterable<String> {
        return Iterable {
            TownyComponent.dataSource!!.towns.map { obj -> obj.name }.iterator()
        }
    }

    companion object {
        fun SetTownColor(sender: CommandSender, town: Town, colors: Array<out String>): Boolean {
            val clrs = arrayOfNulls<TextColor>(colors.size)
            for (i in colors.indices) {
                val c = getColorOrSendError(colors[i], sender) ?: return true
                clrs[i] = c
            }
            var tnc: TextColor?
            val usenc: Boolean = TownColorComponent.component!!.useNationColors
            if (usenc) {
                try {
                    tnc = TownColorComponent.NationColor[town.nation.name.lowercase(Locale.getDefault())]
                } catch (_: Exception) {
                    tnc = null
                }
                if (tnc == null) tnc = NamedTextColor.WHITE //Default nation color - TODO: Make configurable
            } else tnc = null
            for ((key, value) in TownColorComponent.TownColors) {
                var nc: TextColor?
                if (usenc) {
                    nc = try {
                        TownColorComponent.NationColor[TownyComponent.dataSource!!.getTown(key)
                        !!.getNation().name.lowercase(Locale.getDefault())]
                    } catch (_: java.lang.Exception) { //Too lazy for lots of null-checks and it may throw exceptions anyways
                        null
                    }
                    if (nc == null) nc = NamedTextColor.WHITE //Default nation color
                } else nc = null
                if (!usenc || nc == tnc) {
                    var C = 0
                    if (clrs.size == value.size) for (i in clrs.indices) if (clrs[i] == value[i]) C++
                    else break
                    if (C == clrs.size) {
                        sender.sendMessage("§cThis town color combination is already used!")
                        return true
                    }
                }
            }
            TownColorComponent.TownColors[town.name.lowercase(Locale.getDefault())] =
                clrs.filterNotNull().toTypedArray()
            TownyListener.updateTownMembers(town)

            sender.sendMessage("§bColor(s) set.")
            return true
        }

        fun getColorOrSendError(hex: String, sender: CommandSender): TextColor? {
            val c = TextColor.fromHexString(hex)
            if (c == null || c.value() == 0) { // Skip black
                sender.sendMessage("§cThe color '$hex' cannot be found.") //ˇˇ Skip black
                sender.sendMessage("§cUse hex notation, for example: #00FFFF")
            }
            return c
        }

        fun getColorText(col: TextColor): TextComponent {
            return Component.text(col.asHexString(), col)
        }

        fun getTownNameCased(name: String?): String? {
            val town = TownyComponent.dataSource!!.getTown(name) ?: return null
            return town.name
        }
    }
}
