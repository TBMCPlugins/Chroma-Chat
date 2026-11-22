package buttondevteam.chat.components.`fun`

import buttondevteam.core.component.channel.Channel
import buttondevteam.core.component.restart.RestartComponent
import buttondevteam.core.component.restart.ScheduledRestartCommand
import buttondevteam.lib.ScheduledServerRestartEvent
import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.chat.TBMCChatAPI.SendSystemMessage
import buttondevteam.lib.player.ChromaGamerBase
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@CommandClass(
    helpText = [
        "Press",
        "This command resets the restart countdown if it's active. Can only be used once per player.",
        "It's based on Reddit's /r/thebutton"
    ]
)
class PressCommand : ICommand2MC(), Listener {
    private var pressers: HashSet<CommandSender?>? = null //Will be cleared with this class on shutdown/disable
    private var command: ScheduledRestartCommand? = null
    private var startTicks = 0

    @Subcommand
    fun def(sender: CommandSender) {
        if (command == null) {
            sender.sendMessage("§cThe timer isn't ticking... yet.")
            return
        }
        if (pressers!!.contains(sender)) {
            sender.sendMessage("§cYou cannot press more than once.")
            return
        }
        pressers!!.add(sender)
        SendSystemMessage(
            Channel.globalChat,
            Channel.RecipientTestResult.ALL,
            String.format(
                "§b-- %s §bpressed at %.0fs",
                ChromaGamerBase.getFromSender(sender).name,
                command!!.restartCounter / 20f
            ),
            (command!!.component as RestartComponent).restartBroadcast
        )
        command!!.restartCounter = startTicks
    }

    @EventHandler
    fun restartEvent(event: ScheduledServerRestartEvent) {
        command = event.command
        pressers = HashSet()
        startTicks = event.restartTicks
        if (Bukkit.getOnlinePlayers().isNotEmpty()) SendSystemMessage(
            Channel.globalChat,
            Channel.RecipientTestResult.ALL,
            "§b-- Do /press to reset the timer. You may only press once.",
            (command!!.component as RestartComponent).restartBroadcast
        )
    }
}
