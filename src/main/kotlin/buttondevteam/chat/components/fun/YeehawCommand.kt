package buttondevteam.chat.components.`fun`

import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.player.TBMCYEEHAWEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandClass(
    modOnly = false, helpText = [
        "YEEHAW command",
        "This command makes you YEEHAW."
    ]
)
class YeehawCommand : ICommand2MC() {
    @Subcommand
    fun def(sender: CommandSender): Boolean {
        val name = if (sender is Player) sender.displayName() else sender.name()
        val message = name.append(Component.text(" YEEHAWs.", NamedTextColor.AQUA))
        for (p in Bukkit.getOnlinePlayers()) {
            p.playSound(p.location, "tbmc.yeehaw", 1f, 1f)
            p.sendMessage(message) //Not broadcasting, so the Discord plugin can handle the event in a special way
        } // Even a cmdblock could yeehaw in theory

        // Or anyone from Discord
        Bukkit.getPluginManager().callEvent(TBMCYEEHAWEvent(sender))
        return true
    }
}
