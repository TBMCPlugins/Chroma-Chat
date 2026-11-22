package buttondevteam.chat.commands

import buttondevteam.lib.TBMCSystemChatEvent.BroadcastTarget
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.Command2.TextArg
import buttondevteam.lib.chat.Command2MCSender
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.chat.TBMCChatAPI.SendSystemMessage

@CommandClass(
    helpText = ["Me", "Displays a message starting with your name in your current channel."
    ]
)
class MeCommand : ICommand2MC() {
    @Subcommand
    fun def(sender: Command2MCSender, @TextArg message: String?) {
        SendSystemMessage(
            sender.channel,
            sender.channel.getRTR(sender.permCheck),
            "§5* " + sender.sender.name + " " + message,
            BroadcastTarget.ALL
        )
    }
}
