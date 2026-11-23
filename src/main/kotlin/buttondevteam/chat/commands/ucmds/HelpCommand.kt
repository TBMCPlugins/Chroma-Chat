package buttondevteam.chat.commands.ucmds

import buttondevteam.lib.chat.Command2.*
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.CustomTabComplete

@CommandClass(
    modOnly = false, helpText = ["Help", "Prints out help messages for the TBMC plugins"
    ]
)
class HelpCommand : UCommandBase() {
    @Subcommand
    fun def(
        sender: Command2MCSender,
        @TextArg @OptionalArg @CustomTabComplete("commands", "chat") topicOrCommand: String?
    ): Boolean {
        if (topicOrCommand == null) {
            sender.sendMessage(
                arrayOf(
                    "§6---- Chroma Help ----",
                    "Do /u help <topic> for more info",
                    "Do /u help <commandname> [subcommands] for more info about a command",
                    "Topics:",
                    "commands: See all the commands from this plugin",
                    "chat: Shows some info about custom chat features",
                    "colors: Shows Minecraft color codes"
                )
            )
            return true
        }
        if (topicOrCommand.equals("chat", ignoreCase = true)) sender.sendMessage(
            arrayOf(
                "§6---- Chat features ----",
                "- [g] Channel identifier: Click it to copy message", "-- [g] Global chat (/g)",
                "-- [TC] Town chat (/tc)", "-- [NC] Nation chat (/nc)",
                "- Playernames: Hover over them to get some player info",
                "-- Respect: This is the number of paid respects divided by eligible deaths. This is a reference to CoD:AW's \"Press F to pay respects\""
            )
        )
        else if (topicOrCommand.equals("commands", ignoreCase = true)) {
            sender.sendMessage(manager.getCommandList(sender))
        } else {
            val text =
                manager.getCommandNode(topicOrCommand)?.data?.getHelpText(sender) // TODO: This only works for the main command, not subcommands
            if (text == null)  // TODO: Null check for command node
                sender.sendMessage(
                    arrayOf(
                        "§cError: Command not found: $topicOrCommand",
                        "Usage example: /u accept --> /u help u accept"
                    )
                )
            else sender.sendMessage(text)
        }
        return true
    }
}
