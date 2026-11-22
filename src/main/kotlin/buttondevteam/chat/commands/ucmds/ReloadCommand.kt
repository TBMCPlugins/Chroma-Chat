package buttondevteam.chat.commands.ucmds

import buttondevteam.chat.PluginMain
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import org.bukkit.command.CommandSender

@CommandClass(
    helpText = ["Reload", "Reloads the config"
    ], modOnly = true
)
class ReloadCommand : UCommandBase() {
    @Subcommand
    fun def(sender: CommandSender) {
        if (PluginMain.Companion.Instance.tryReloadConfig()) sender.sendMessage("§bReloaded config")
        else sender.sendMessage("§cFailed to reload config.")
    }
}
