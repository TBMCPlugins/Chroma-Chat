package buttondevteam.chat.components.`fun`

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.PluginMain
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import buttondevteam.lib.player.TBMCPlayerBase
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


@CommandClass(
    helpText = ["F Top" //
        , "Shows the respect leaderboard" //
    ]
)
class FTopCommand : ICommand2MC() {
    private val playerdir = java.io.File("TBMC/players/")
    private var cached = arrayOf<ChatPlayer>()
    private var lastcache: Long = 0

    @Command2.Subcommand
    fun def(sender: org.bukkit.command.CommandSender, @Command2.OptionalArg page: Int): Boolean {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(PluginMain.Instance!!, Runnable {
            if (lastcache < System.nanoTime() - 60000000000L) { // 1m
                cached = playerdir.listFiles()?.filter { f -> f.getName().length > 4 }
                    ?.map { f ->
                        try {
                            return@map TBMCPlayerBase.getPlayer(
                                UUID.fromString(f!!.getName().substring(0, f.getName().length - 4)),
                                ChatPlayer::class.java
                            )
                        } catch (_: java.lang.Exception) {
                            return@map null
                        }
                    }
                    ?.filterNotNull()
                    ?.sortedBy { it.f }
                    ?.toTypedArray() ?: emptyArray() // TODO: Properly implement getting all players
                lastcache = System.nanoTime()
            }
            val i = try {
                if (page < 1) 1 else page
            } catch (_: java.lang.Exception) {
                1
            }
            val ai = AtomicInteger()
            sender.sendMessage("§6---- Top Fs ----")
            sender.sendMessage(
                cached.drop((i - 1) * 10).take(i * 10)
                    .joinToString("\n")
                    { cp -> String.format("%d. %s - %f.2", ai.incrementAndGet(), cp.playerName, cp.f) })
        })
        return true
    }
}
