package buttondevteam.chat.components.announce

import buttondevteam.chat.commands.ucmds.UCommandBase
import buttondevteam.chat.components.formatter.FormatterComponent
import buttondevteam.core.ComponentManager
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandClass(modOnly = true)
class AnnounceCommand(private val _component: AnnouncerComponent) : UCommandBase() {

    @Command2.Subcommand(
        helpText = [
            "Add announcement",
            "This command adds a new announcement"
        ]
    )
    fun add(sender: CommandSender, @Command2.TextArg text: kotlin.String): kotlin.Boolean {
        val finalmessage = text.replace('&', '§')
        _component.announceMessages.add(finalmessage)
        sender.sendMessage("§bAnnouncement added.§r")
        return true
    }

    @Command2.Subcommand(
        helpText = [
            "Edit announcement",
            "This command lets you edit an announcement by its index.",
            "Shouldn't be used directly, use either command blocks or click on an announcement in /u announce list instead."
        ]
    )
    fun edit(sender: CommandSender, index: Byte, @Command2.TextArg text: String): Boolean {
        val finalmessage1 = text.replace('&', '§')
        if (index > 100) return false
        while (_component.announceMessages.size <= index) _component.announceMessages.add("")
        _component.announceMessages[index.toInt()] = finalmessage1
        sender.sendMessage("Announcement edited.")
        return true
    }

    @Command2.Subcommand(
        helpText = [
            "List announcements",
            "This command lists the announcements and the time between them"
        ]
    )
    fun list(sender: CommandSender): Boolean {
        sender.sendMessage("§bList of announce messages:§r")
        sender.sendMessage("§bFormat: [index] message§r")
        var i = 0
        for (message in _component.announceMessages) {
            val msg = "[${i++}] $message"
            if (!ComponentManager.isEnabled(FormatterComponent::class.java) || !Bukkit.getOnlinePlayers()
                    .contains(sender)
            ) {
                sender.sendMessage(msg)
                continue
            }
            val command = "/$commandPath edit ${i - 1} ${message.replace('§', '&')}"
            sender.sendMessage(
                Component.text(msg)
                    .hoverEvent(HoverEvent.showText(Component.text("Click to edit")))
                    .clickEvent(ClickEvent.suggestCommand(command))
            )
        }
        sender.sendMessage("§bCurrent wait time between announcements: ${_component.announceTime / 60 / 1000} minute(s)§r")
        return true
    }

    @Command2.Subcommand(
        helpText = [
            "Remove announcement",
            "This command removes an announcement"
        ]
    )
    fun remove(sender: CommandSender, index: Int): Boolean {
        val msgs = _component.announceMessages
        if (index < 0 || index > msgs.size) return false
        msgs.removeAt(index)
        sender.sendMessage("Announcement removed.")
        return true
    }

    @Command2.Subcommand(
        helpText = [
            "Set time",
            "This command sets the time between the announcements"
        ]
    )
    fun settime(sender: CommandSender, minutes: Int): Boolean {
        _component.announceTime = minutes * 60 * 1000
        sender.sendMessage("Time set between announce messages to $minutes minutes")
        return true
    }
}
