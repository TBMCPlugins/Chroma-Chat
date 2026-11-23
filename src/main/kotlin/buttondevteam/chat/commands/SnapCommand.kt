package buttondevteam.chat.commands

import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.*


@CommandClass(
    modOnly = true, helpText = ["Snap", "Perfectly balanced as all things should be."
    ]
)
class SnapCommand : ICommand2MC() {
    @Command2.Subcommand
    fun def(sender: CommandSender) {
        val pls = Bukkit.getOnlinePlayers().toMutableList()
        var target = pls.size / 2
        val rand = Random()
        if (pls.remove(sender) && target > 0) target-- //The sender isn't kicked, so kick someone else

        while (pls.size > target) pls.removeAt(rand.nextInt(pls.size)).kickPlayer("You were saved by Thanos.")
        sender.sendMessage("$target grateful players remain.")
    }
}
