package buttondevteam.chat.commands.ucmds.admin

import buttondevteam.chat.PluginMain
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import org.bukkit.command.CommandSender

@CommandClass(
    helpText = ["Debug mode", "Toggles debug mode, which prints debug messages to the console."
    ]
)
class DebugCommand : AdminCommandBase() {
    @Subcommand
    fun def(sender: CommandSender): Boolean {
        sender.sendMessage("§eDebug mode " + (if ((!DebugMode).also {
                DebugMode = it
            }) "§aenabled." else "§cdisabled."))
        return true
    }

    companion object {
        @JvmField
        var DebugMode: Boolean = false

        fun SendDebugMessage(message: String?) {
            if (DebugMode) if (PluginMain.Instance != null) PluginMain.Instance!!.logger
                .info(message)
            else println(message)
        }
    }
}
