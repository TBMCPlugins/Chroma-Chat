package buttondevteam.chat.components.formatter

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.ChatUtils
import buttondevteam.chat.PluginMain
import buttondevteam.chat.VanillaUtils
import buttondevteam.chat.commands.ucmds.admin.DebugCommand
import buttondevteam.chat.components.chatonly.ChatOnlyComponent
import buttondevteam.chat.components.formatter.formatting.*
import buttondevteam.chat.components.`fun`.FunComponent
import buttondevteam.chat.components.towny.TownyComponent
import buttondevteam.chat.listener.PlayerListener
import buttondevteam.core.ComponentManager
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.ChromaUtils
import buttondevteam.lib.ChromaUtils.asString
import buttondevteam.lib.TBMCChatEvent
import buttondevteam.lib.TBMCChatEventBase
import buttondevteam.lib.TBMCCoreAPI
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayerBase
import com.earth2me.essentials.User
import net.ess3.api.events.AfkStatusChangeEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.Locale.getDefault
import java.util.concurrent.atomic.AtomicInteger

object ChatProcessing {
    private val HASHTAG_PATTERN: java.util.regex.Pattern = java.util.regex.Pattern.compile("#(\\w+)")
    private val URL_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("(http[\\w:/?=$\\-_.+!*'(),&]+(?:#[\\w]+)?)")
    private val MASKED_LINK_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("\\[([^\\[\\]]+)]\\(([^()]+)\\)")
    private val RainbowPresserColors: Array<NamedTextColor> =
        arrayOf(
            NamedTextColor.RED,
            NamedTextColor.GOLD,
            NamedTextColor.YELLOW,
            NamedTextColor.GREEN,
            NamedTextColor.BLUE,
            NamedTextColor.DARK_PURPLE
        )
    private val WORD_PATTERN: java.util.regex.Pattern = java.util.regex.Pattern.compile("\\S+")
    private val GREENTEXT_PATTERN: java.util.regex.Pattern = java.util.regex.Pattern.compile("^>(?:.|\\s)*")
    private var pingedconsole = false

    private val commonFormatters: List<MatchProviderBase> =
        com.google.common.collect.Lists.newArrayList(
            RangeMatchProvider("bold", "**", FormatSettings(bold = true)),
            RangeMatchProvider("italic", "*", FormatSettings(italic = true)),
            RangeMatchProvider("underlined", "__", FormatSettings(underlined = true)),
            RangeMatchProvider("italic2", "_", FormatSettings(italic = true)),
            RangeMatchProvider("strikethrough", "~~", FormatSettings(strikethrough = true)),
            RangeMatchProvider(
                "spoiler", "||", FormatSettings(
                    obfuscated = true,
                    onmatch = { match, cf, _ ->
                        cf.hoverText = match
                        match
                    })
            ),
            StringMatchProvider(
                "nullMention",
                FormatSettings(color = NamedTextColor.DARK_RED),
                true,
                "null"
            ),  // Properly added a bug as a feature
            StringMatchProvider(
                "consolePing", FormatSettings(color = NamedTextColor.AQUA, onmatch = { _, _, _ ->
                    if (!pingedconsole) {
                        print("\u0007")
                        pingedconsole = true // Will set it to false in ProcessChat
                        }
                        "@console"
                }), true, "@console"
            ),

            StringMatchProvider(
                "cyan",
                FormatSettings(color = NamedTextColor.AQUA),
                true,
                "cyan"
            ),  // #55
            RangeMatchProvider(
                "code",
                "`",
                FormatSettings(color = NamedTextColor.DARK_GRAY)
            ),
            RegexMatchProvider(
                "maskedLink", MASKED_LINK_PATTERN, FormatSettings(
                    underlined = true,
                    onmatch = { _, builder, section ->
                        val text = section.Matches[0]
                        val link = section.Matches[1]

                        builder.openlink = link
                        text
                    }
                )),
            RegexMatchProvider("url", URL_PATTERN, FormatSettings(underlined = true, openlink = "$1")),
            RegexMatchProvider(
                "hashtag", HASHTAG_PATTERN, FormatSettings(
                    color = NamedTextColor.BLUE,
                    openlink = "https://twitter.com/hashtag/$1"
                )
            ),
            StringMatchProvider(
                "someone", FormatSettings(color = NamedTextColor.AQUA, onmatch = onmatch@{ match, _, _ ->
                    if (Bukkit.getOnlinePlayers().isEmpty()) return@onmatch match
                    val players = Bukkit.getOnlinePlayers().toList()
                    val playerC = Random().nextInt(players.size)
                    val player: Player = players[playerC]
                    playPingSound(player, ComponentManager.getIfEnabled(FormatterComponent::class.java))
                    "@someone (" + player.displayName + "§r)"
                }), true, "@someone"
            ),
            RegexMatchProvider("greentext", GREENTEXT_PATTERN, FormatSettings(color = NamedTextColor.GREEN))
        )
    private val testPlayers = arrayOf("Koiiev", "iie", "Alisolarflare", "NorbiPeti", "Arsen_Derby_FTW", "carrot_lynx")

    fun ProcessChat(e: TBMCChatEvent, component: FormatterComponent): Boolean {
        val channel: Channel = e.channel
        val cuser: ChromaGamerBase = e.user
        val message: String = e.message.asString() // TODO: Deal with existing components?
        val processstart = System.nanoTime()
        val player: Player? = (if (cuser is TBMCPlayerBase) cuser.player else null)
        val user: User? = PluginMain.essentials!!.getUser(player)

        if (user != null && PluginMain.essentials!!.settings.cancelAfkOnInteract()) {
            user.updateActivity(true, AfkStatusChangeEvent.Cause.CHAT) //Could talk in a private channel, so broadcast
            if (user.isMuted) return true
        }

        // Due to the online player map, getPlayer() can be more efficient than getAs()
        val mp = if (player != null) TBMCPlayerBase.getPlayer(player.uniqueId, ChatPlayer::class.java)
        else e.user.getAs(ChatPlayer::class.java) //May be null


        if (mp != null) {
            if (System.nanoTime() - mp.LastMessageTime < 1000L * 1000L * component.minTimeBetweenMessages) { //0.1s by default
                cuser.sendMessage("§cYou are sending messages too quickly!")
                return true
            }
            mp.LastMessageTime = System.nanoTime()
        }

        doFunStuff(cuser, e, message)

        val channelidentifier = getChannelID(channel, e.origin)

        val colorModeChanged = mp?.OtherColorMode != null
        val colormode = mp?.OtherColorMode ?: channel.color

        val formatters = if (component.allowFormatting) {
            val fs = addFormatters({ sender -> e.shouldSendTo(ChromaGamerBase.getFromSender(sender)) }, component)
            if (colorModeChanged && mp.RainbowPresserColorMode) { // Only overwrite channel color
                createRPC(colormode, fs)
            }
            pingedconsole = false // Will set it to true onmatch (static constructor)
            fs
        } else mutableListOf()

        val builder = createEmptyMessageLine(cuser, message, player, channelidentifier, e.origin)
        var combinetime = System.nanoTime()
        ChatFormatter.Combine(formatters, message, builder, component.config, FormatSettings(color = colormode))
        combinetime = System.nanoTime() - combinetime

        try {
            if (!channel.isGlobal) {
                val senderGroup: String? = e.getGroupID(cuser)
                if (senderGroup == null) { // Never send messages if the group is null
                    cuser.sendMessage("§cYou don't have permission to send this message or something went wrong")
                    return true
                }
                val tc = ComponentManager.getIfEnabled(TownyComponent::class.java)
                var spyConsumer: java.util.function.Consumer<Player?>? = null
                if (tc != null) spyConsumer = tc.handleSpiesInit(channel, builder)
                for (p in Bukkit.getOnlinePlayers()) {
                    val group: String?
                    if (player != null
                        && PluginMain.essentials!!.getUser(p).isIgnoredPlayer(PluginMain.essentials!!.getUser(player))
                    ) group = null // Don't send the message to them
                    else group = VanillaUtils.getGroupIfChatOn(p, e)
                    if (senderGroup == group) {
                        p.sendMessage(builder.build())
                        if (tc != null) spyConsumer!!.accept(p)
                    }
                    //Only sends if didn't send normally
                }
            } else for (p in Bukkit.getOnlinePlayers()) p.sendMessage(builder.build())

            PluginMain.Instance!!.server.consoleSender.sendMessage(builder.build())
        } catch (ex: Exception) {
            TBMCCoreAPI.SendException("An error occured while sending a chat message!", ex, PluginMain.Instance!!)
            cuser.sendMessage("§cAn error occured while sending the message.")
            return true
        }
        DebugCommand.SendDebugMessage(
            "-- Full ChatProcessing time: " + (System.nanoTime() - processstart) / 1000000f + " ms"
        )
        DebugCommand.SendDebugMessage("-- ChatFormatter.Combine time: " + combinetime / 1000000f + " ms")
        return false
    }

    fun createRPC(colormode: TextColor, formatters: MutableList<MatchProviderBase>) {
        val rpc = AtomicInteger(0)
        formatters.add(
            RegexMatchProvider(
                "rpc",
                WORD_PATTERN,
                FormatSettings(color = colormode, onmatch = { match, cf, _ ->
                    cf.color = RainbowPresserColors[rpc.getAndUpdate { i: Int ->
                        var i = i
                        if (++i < RainbowPresserColors.size) i else 0
                    }]
                    match
                })
            )
        )
    }

    fun createEmptyMessageLine(
        user: ChromaGamerBase, message: String, player: Player?,
        channelidentifier: String, origin: String
    ): TextComponent.Builder {
        val json = Component.text()
        ChatOnlyComponent.tellrawCreate(user.getAs(ChatPlayer::class.java), json)
        val channelHover = (if (ChatUtils.MCORIGIN == origin) "" else "From $origin\n") + "Copy message"
        json.append(
            Component.text(channelidentifier)
                .hoverEvent(
                    HoverEvent.hoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.text(channelHover).color(NamedTextColor.BLUE)
                    )
                ).clickEvent(ClickEvent.suggestCommand(message))
        )
        if (player != null) {
            if (PluginMain.permission!!.has(player, "tbmc.badge.diamond"))  // TODO: Cross-platform permissions
                json.append(
                    Component.text("[P]")
                        .color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD)
                        .hoverEvent(
                            HoverEvent.hoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.text("Diamond Patreon supporter")
                            )
                        )
                )
            else if (PluginMain.permission!!.has(player, "tbmc.badge.gold")) json.append(
                Component.text("[P]")
                    .color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                    .hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text("Gold Patreon supporter")
                        )
                    )
            )
        }
        json.append(Component.text(" <"))
        json.append(
            Component.text(user.name).hoverEvent(
                HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.text(user.getInfo(ChromaGamerBase.InfoTarget.MCHover))
                )
            )
        )
        json.append(Component.text("> "))
        return json
    }

    @JvmStatic
    fun getChannelID(channel: Channel, origin: String): String {
        return (("[" + (if (ChatUtils.MCORIGIN == origin) "" else "§8" + origin[0] + "§r|") + channel.displayName) + "]")
    }

    fun addFormatters(
        canSee: (Player) -> Boolean,
        component: FormatterComponent?
    ): MutableList<MatchProviderBase> {
        val formatters = commonFormatters.toMutableList()

        val nottest = Bukkit.getOnlinePlayers().isNotEmpty()
        if (nottest || ChromaUtils.isTest) {
            val names = if (nottest) Bukkit.getOnlinePlayers().filter(canSee).map { it.name }.toTypedArray()
            else testPlayers.copyOf()
            val nicknames = Bukkit.getOnlinePlayers().filter(canSee)
                .map { obj -> obj.uniqueId }
                .mapNotNull { key: UUID -> PlayerListener.nicknames.inverse()[key] }
                .toTypedArray()

            val error = { message: String ->
                val instance = PluginMain.Instance
                if (instance != null) instance.logger.warning(message)
                else println(message)
            }

            if (names.isNotEmpty())  //Add as first so it handles special characters (_) - though the order of the different types are defined
                formatters.add(
                    0,
                    StringMatchProvider(
                        "name", FormatSettings(
                            color = NamedTextColor.AQUA,
                            onmatch = onmatch@{ match, _, _ ->
                                val color: String
                                val name: String
                                if (nottest) {
                                    val player = Bukkit.getPlayer(match)
                                    if (player == null) {
                                        error("Error: Can't find player $match but was reported as online.")
                                        return@onmatch "§c$match§r"
                                    }
                                    val mpp = TBMCPlayerBase.getPlayer(player.uniqueId, ChatPlayer::class.java)
                                    playPingSound(player, component)
                                    // TODO: This doesn't work until the 'new' component stuff
                                    color = String.format(
                                        "§%x",
                                        (if (mpp.GetFlairColor() == null) 0xb else mpp.GetFlairColor())
                                    )
                                    name = player.name
                                } else {
                                    val testName = testPlayers.find { tp -> tp.equals(match, ignoreCase = true) }
                                    if (testName == null) {
                                        error("Error: Can't find test player $match.")
                                        return@onmatch "§c$match§r"
                                    }
                                    color = String.format("§%x", 0xb)
                                    name = testName
                                }
                                "$color$name§r" //Fix name casing, except when testing
                            }), true, *names
                    )
                )

            if (nicknames.isNotEmpty())  //Add as first so it handles special characters
                formatters.add(
                    0,
                    StringMatchProvider(
                        "nickname", FormatSettings(
                            color = NamedTextColor.AQUA,
                            onmatch = onmatch@{ match, _, _ ->
                                //Made a stream and all that but I can actually store it lowercased
                                val lmatch = match.lowercase(getDefault())
                                if (PlayerListener.nicknames.containsKey(lmatch)) {
                                    val p = Bukkit.getPlayer(PlayerListener.nicknames[lmatch]!!)
                                    if (p == null) {
                                        error(("Error: Can't find player nicknamed $lmatch but was reported as online."))
                                        return@onmatch "§c$match§r"
                                    }
                                    playPingSound(p, component)
                                    return@onmatch PluginMain.essentials!!.getUser(p).nickname
                                }
                                error(("Player nicknamed $lmatch not found in nickname map but was reported as online."))
                                "§c$match§r"
                            }), true, *nicknames
                    )
                )
        }
        return formatters
    }

    private fun playPingSound(p: Player, component: FormatterComponent?) {
        if (component == null || component.notificationSound.isEmpty()) p.playSound(
            p.location,
            org.bukkit.Sound.ENTITY_ARROW_HIT_PLAYER,
            1.0f,
            0.5f
        ) // TODO: Airhorn
        else p.playSound(p.location, component.notificationSound, 1.0f, component.notificationPitch)
    }

    fun doFunStuff(user: ChromaGamerBase, event: TBMCChatEventBase, message: String) {
        val fc = ComponentManager.getIfEnabled(FunComponent::class.java)
        fc?.onChat(user, event, message)
    }
}
