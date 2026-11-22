package buttondevteam.chat.components.towncolors

import buttondevteam.chat.commands.ucmds.UCommandBase
import buttondevteam.chat.components.towncolors.admin.TownColorCommand
import buttondevteam.chat.components.towny.TownyComponent
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException
import com.palmergames.bukkit.towny.`object`.Resident
import org.bukkit.entity.Player

@CommandClass(
    helpText = [
        "Town Color",
        "This command allows setting a color for a town.",
        "The town will be shown with this color on Dynmap and all players in the town will appear in chat with these colors.",
        "The colors will split the name evenly but residents can override that with /u ncolor."
    ]
)
class TownColorCommand : UCommandBase() {
    @Command2.Subcommand
    fun def(player: Player, vararg colornames: String): Boolean {
        val msg = "§cYou need to be the mayor of a town to set its colors."
        try {
            val res: Resident? = TownyComponent.dataSource!!.getResident(player.name)
            if (!res!!.isMayor) {
                player.sendMessage(msg)
                return true
            }
            val cc = TownColorComponent.component!!.colorCount
            if (colornames.size > cc) {
                player.sendMessage("You can only use " + cc + " color" + (if (cc > 1) "s" else "") + ".")
                return true
            }
            val t = res.town
            return TownColorCommand.SetTownColor(player, t, colornames)
        } catch (_: NotRegisteredException) {
            player.sendMessage(msg)
            return true
        }
    }
}
