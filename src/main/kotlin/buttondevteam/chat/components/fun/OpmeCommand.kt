package buttondevteam.chat.components.`fun`

import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import org.bukkit.command.CommandSender

@CommandClass(
    modOnly = false, path = "u opme", helpText = [
        "OP me",
        "Totally makes you OP"
    ]
)
class OpmeCommand : ICommand2MC() {
    @Subcommand
    fun def(sender: CommandSender): Boolean {
        sender.sendMessage("It would be nice, wouldn't it?")
        return true
    }
}
