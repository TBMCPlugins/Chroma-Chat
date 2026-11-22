package buttondevteam.chat.components.towncolors.admin

import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase
import buttondevteam.chat.components.towncolors.TownColorComponent
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass


@CommandClass(
    helpText = [
        "Town Color Count",
        "Sets how many colors can be used for a town."
    ]
)
class TCCount : AdminCommandBase() {
    @Command2.Subcommand
    fun def(sender: org.bukkit.command.CommandSender, count: kotlin.Byte): kotlin.Boolean {
        val comp = component as TownColorComponent
        comp.colorCount = count
        sender.sendMessage("Color count set to $count")
        return true
    }
}
