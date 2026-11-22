package buttondevteam.chat.components.towncolors

import buttondevteam.chat.commands.ucmds.UCommandBase
import buttondevteam.chat.components.towncolors.admin.NationColorCommand
import buttondevteam.chat.components.towny.TownyComponent
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException
import org.bukkit.entity.Player

@CommandClass(
    helpText = ["Nation Color" //
        , "This command allows setting a color for a nation." //
        , "Each town in the nation will have it's first color (border) set to this color." //
        , "See the help text for /u towncolor for more details." //
    ]
)
class NationColorCommand : UCommandBase() {
    @Subcommand
    fun def(player: Player, color: String?): Boolean {
        val msg = "§cYou need to be the king of a nation to set it's colors."
        try {
            val res = TownyComponent.dataSource!!.getResident(player.name)
            if (!res!!.isKing) {
                player.sendMessage(msg)
                return true
            }
            val n = res.town.nation
            return NationColorCommand.SetNationColor(player, n, color)
        } catch (e: NotRegisteredException) {
            player.sendMessage(msg)
            return true
        }
    }
}
