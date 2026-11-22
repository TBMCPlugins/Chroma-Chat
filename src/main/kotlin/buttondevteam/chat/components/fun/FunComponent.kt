package buttondevteam.chat.components.`fun`

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.PluginMain
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.TBMCChatEventBase
import buttondevteam.lib.TBMCCommandPreprocessEvent
import buttondevteam.lib.TBMCSystemChatEvent
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.chat.TBMCChatAPI.SendSystemMessage
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayerBase
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import java.util.*

/**
 * Random things I added over the years.
 */
class FunComponent : Component<PluginMain>(), Listener {
    private var ActiveF = false
    private var FPlayer: ChatPlayer? = null
    private var Ftask: BukkitTask? = null
    private val Fs: HashSet<ChromaGamerBase?> = HashSet<ChromaGamerBase?>()
    private var command: UnlolCommand? = null
    private var unlolTarget: TBMCSystemChatEvent.BroadcastTarget? = null
    private var fTarget: TBMCSystemChatEvent.BroadcastTarget? = null
    private val random = Random()

    /**
     * The strings that count as laughs, see unlol.
     */
    private val laughStrings by config.getListData(
        "laughStrings",
        mutableListOf("xd", "lel", "lawl", "kek", "lmao", "hue", "hah", "rofl")
    )

    /**
     * The "Press F to pay respects" meme in Minecraft. It will randomly appear on player death and keep track of how many "F"s are said in chat.
     * You can hover over a player's name to see their respect.
     */
    private val respect by config.getData("respect", true)

    /**
     * This is an inside joke on our server.
     * It keeps track of laughs (lols and what's defined in laughStrings) and if someone does /unlol or /unlaugh it will unlaugh the last person who laughed.
     * Which also blinds the laughing person for a few seconds. This action can only be performed once per laugh.
     */
    private val unlol by config.getData("unlol", true)

    override fun enable() {
        unlolTarget = TBMCSystemChatEvent.BroadcastTarget.add("unlol")
        fTarget = TBMCSystemChatEvent.BroadcastTarget.add("respect")
        val pc = PressCommand()
        registerCommand(pc)
        registerListener(pc)
        registerCommand(UnlolCommand(unlolTarget!!).also { command = it })
        registerListener(this)
        registerCommand(FTopCommand())
        registerCommand(OpmeCommand())
        registerCommand(YeehawCommand())
        registerCommand(CCommand())
    }

    override fun disable() {
    }

    fun onChat(sender: ChromaGamerBase, event: TBMCChatEventBase, message: String) {
        if (ActiveF && !Fs.contains(sender) && message.equals("F", ignoreCase = true)) Fs.add(sender)

        if (unlol) {
            val msg = message.lowercase(Locale.getDefault())
            val lld = UnlolCommand.LastlolData(sender, event, System.nanoTime())
            var add = msg.contains("lol")
            if (add) lld.Lolornot = true
            else {
                val laughs = laughStrings
                for (laugh in laughs) {
                    add = msg.contains(laugh)
                    if (add) {
                        lld.Lolornot = false
                        break
                    }
                }
            }
            if (add) command!!.Lastlol[event.channel] = lld
        }
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        if (e.deathMessage!!.isNotEmpty() && respect && random.nextBoolean()) { // Don't store Fs for NPCs
            val tt = Runnable {
                if (ActiveF) {
                    ActiveF = false
                    val fPlayer = FPlayer
                    if (fPlayer != null && fPlayer.FCount < Int.MAX_VALUE - 1) fPlayer.FCount = fPlayer.FCount + Fs.size
                    SendSystemMessage(
                        Channel.globalChat, Channel.RecipientTestResult.ALL,
                        ("§b" + Fs.size + " " + (if (Fs.size == 1) "person" else "people")
                            + " paid their respects.§r"), fTarget
                    )
                    Fs.clear()
                }
            }
            if (Ftask != null) {
                Ftask!!.cancel()
                tt.run() //Finish previous one
            }
            ActiveF = true
            Fs.clear()
            FPlayer = TBMCPlayerBase.getPlayer(e.getEntity().uniqueId, ChatPlayer::class.java).also { it.FDeaths++ }
            SendSystemMessage(
                Channel.globalChat, Channel.RecipientTestResult.ALL,
                "§bPress F to pay respects.§r", fTarget
            )
            Ftask = Bukkit.getScheduler().runTaskLaterAsynchronously(PluginMain.Instance!!, tt, 15 * 20L)
        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (unlol) command!!.Lastlol.values.removeIf { lld ->
            lld.Lolowner == ChromaGamerBase.getFromSender(event.getPlayer())
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCommandPreprocess(event: TBMCCommandPreprocessEvent) {
        if (event.isCancelled()) return
        if (!unlol) return
        val cmd = event.message
        // We don't care if we have arguments
        if (cmd.lowercase(Locale.getDefault()).startsWith("/un")) {
            for (ht in PluginMain.Instance!!.server.helpMap.helpTopics) {
                if (ht.getName().equals(cmd, ignoreCase = true)) return
            }
            val user = event.sender
            if (user !is TBMCPlayerBase) {
                // TODO: Cross-platform permission check; console is not supported here
                user.sendMessage("§cError: You must be a player to use this command.")
                return
            }
            val player = user.player
            if (player != null && PluginMain.permission!!.has(player, "chroma.unanything")) {
                event.isCancelled = true
                val index = cmd.lastIndexOf(' ')
                if (index == -1) {
                    event.sender.sendMessage("§cUsage: $cmd <player>")
                    return
                }
                val s = cmd.substring(3, index)
                val target = Bukkit.getPlayer(cmd.substring(index + 1))
                if (target == null) {
                    event.sender.sendMessage("§cError: Player not found. (/un$s <player>)")
                    return
                }
                target.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 5, false, false))
                val chan = user.channel
                SendSystemMessage(
                    chan, chan.getRTR(event.sender), (event.sender.name + " un" + s
                        + "'d " + target.displayName), unlolTarget
                )
            }
        }
    }
}
