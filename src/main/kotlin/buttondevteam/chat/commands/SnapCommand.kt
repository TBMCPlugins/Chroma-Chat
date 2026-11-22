package buttondevteam.chat.commands


@CommandClass(
    modOnly = true, helpText = ["Snap", "Perfectly balanced as all things should be."
    ]
)
class SnapCommand : ICommand2MC() {
    @Subcommand
    fun def(sender: org.bukkit.command.CommandSender) {
        val pls = java.util.ArrayList<org.bukkit.entity.Player?>(org.bukkit.Bukkit.getOnlinePlayers())
        var target: kotlin.Int = pls.size() / 2
        val rand = java.util.Random()
        if (pls.remove(sender) && target > 0) target-- //The sender isn't kicked, so kick someone else

        while (pls.size() > target) pls.remove(rand.nextInt(pls.size())).kickPlayer("You were saved by Thanos.")
        sender.sendMessage(target.toString() + " grateful players remain.")
    }
}
