package buttondevteam.chat.components.formatter


object ChatProcessing {
    private val HASHTAG_PATTERN: java.util.regex.Pattern = java.util.regex.Pattern.compile("#(\\w+)")
    private val URL_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("(http[\\w:/?=$\\-_.+!*'(),&]+(?:#[\\w]+)?)")
    private val MASKED_LINK_PATTERN: java.util.regex.Pattern =
        java.util.regex.Pattern.compile("\\[([^\\[\\]]+)]\\(([^()]+)\\)")
    private val RainbowPresserColors: kotlin.Array<net.kyori.adventure.text.format.NamedTextColor?> =
        kotlin.arrayOf<net.kyori.adventure.text.format.NamedTextColor>(
            net.kyori.adventure.text.format.NamedTextColor.RED,
            net.kyori.adventure.text.format.NamedTextColor.GOLD,
            net.kyori.adventure.text.format.NamedTextColor.YELLOW,
            net.kyori.adventure.text.format.NamedTextColor.GREEN,
            net.kyori.adventure.text.format.NamedTextColor.BLUE,
            net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
        )
    private val WORD_PATTERN: java.util.regex.Pattern = java.util.regex.Pattern.compile("\\S+")
    private val GREENTEXT_PATTERN: java.util.regex.Pattern = java.util.regex.Pattern.compile("^>(?:.|\\s)*")
    private var pingedconsole = false

    private val commonFormatters: java.util.ArrayList<MatchProviderBase?> =
        com.google.common.collect.Lists.newArrayList<MatchProviderBase>(
            RangeMatchProvider("bold", "**", builder().bold(true).build()),
            RangeMatchProvider("italic", "*", builder().italic(true).build()),
            RangeMatchProvider("underlined", "__", builder().underlined(true).build()),
            RangeMatchProvider("italic2", "_", builder().italic(true).build()),
            RangeMatchProvider("strikethrough", "~~", builder().strikethrough(true).build()),
            RangeMatchProvider(
                "spoiler", "||", builder().obfuscated(true)
                    .onmatch({ match, cf, fs ->
                        cf.setHoverText(match)
                        match
                    }).build()
            ),
            StringMatchProvider(
                "nullMention",
                builder().color(net.kyori.adventure.text.format.NamedTextColor.DARK_RED).build(),
                true,
                "null"
            ),  // Properly added a bug as a feature
            StringMatchProvider(
                "consolePing", builder().color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                    .onmatch({ match, builder, section ->
                        if (!ChatProcessing.pingedconsole) {
                            kotlin.io.print("\u0007")
                            ChatProcessing.pingedconsole = true // Will set it to false in ProcessChat
                        }
                        "@console"
                    }).build(), true, "@console"
            ),

            StringMatchProvider(
                "cyan",
                builder().color(net.kyori.adventure.text.format.NamedTextColor.AQUA).build(),
                true,
                "cyan"
            ),  // #55
            RangeMatchProvider(
                "code",
                "`",
                builder().color(net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY).build()
            ),
            RegexMatchProvider(
                "maskedLink", ChatProcessing.MASKED_LINK_PATTERN, builder().underlined(true)
                    .onmatch({ match, builder, section ->
                        val text: kotlin.String
                        val link: kotlin.String
                        if (section.Matches.size() < 2 || (section.Matches.get(0)
                                .also { text = it }).length == 0 || (section.Matches.get(1)
                                .also { link = it }).length == 0
                        ) return@onmatch "[MISSING LINK]" //Doesn't actually happen, because of the regex

                        builder.setOpenlink(link)
                        text
                    }).build()
            ),
            RegexMatchProvider("url", ChatProcessing.URL_PATTERN, builder().underlined(true).openlink("$1").build()),
            RegexMatchProvider(
                "hashtag",
                ChatProcessing.HASHTAG_PATTERN,
                builder().color(net.kyori.adventure.text.format.NamedTextColor.BLUE)
                    .openlink("https://twitter.com/hashtag/$1").build()
            ),
            StringMatchProvider(
                "someone", builder().color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                    .onmatch({ match, builder, section ->
                        if (org.bukkit.Bukkit.getOnlinePlayers().size == 0) return@onmatch match
                        val players: com.google.common.collect.ImmutableList<out org.bukkit.entity.Player> =
                            com.google.common.collect.ImmutableList.copyOf(org.bukkit.Bukkit.getOnlinePlayers())
                        val playerC = java.util.Random().nextInt(players.size)
                        val player: org.bukkit.entity.Player = players.get(playerC)
                        ChatProcessing.playPingSound(
                            player,
                            getIfEnabled<FormatterComponent?>(FormatterComponent::class.java)
                        )
                        "@someone (" + player.getDisplayName() + "§r)"
                    }).build(), true, "@someone"
            ),
            RegexMatchProvider(
                "greentext",
                ChatProcessing.GREENTEXT_PATTERN,
                builder().color(net.kyori.adventure.text.format.NamedTextColor.GREEN).build()
            )
        )
    private val testPlayers =
        kotlin.arrayOf<kotlin.String?>("Koiiev", "iie", "Alisolarflare", "NorbiPeti", "Arsen_Derby_FTW", "carrot_lynx")

    fun ProcessChat(e: TBMCChatEvent, component: FormatterComponent): kotlin.Boolean {
        val channel: buttondevteam.core.component.channel.Channel = e.channel
        val cuser: ChromaGamerBase = e.user
        val message: kotlin.String = e.message
        val processstart = java.lang.System.nanoTime()
        val player: org.bukkit.entity.Player? =
            (if (cuser is TBMCPlayerBase) (cuser as TBMCPlayerBase).player else null)
        val user: com.earth2me.essentials.User = PluginMain.Companion.essentials.getUser(player)

        if (player != null && PluginMain.Companion.essentials.getSettings().cancelAfkOnInteract()) {
            user.updateActivity(true, AfkStatusChangeEvent.Cause.CHAT) //Could talk in a private channel, so broadcast
            if (user.isMuted()) return true
        }

        val mp: ChatPlayer?
        if (player != null) mp =
            TBMCPlayerBase.Companion.getPlayer<ChatPlayer?>(player.getUniqueId(), ChatPlayer::class.java)
        else  //Due to the online player map, getPlayer() can be more efficient than getAs()
            mp = e.user.getAs<ChatPlayer?>(ChatPlayer::class.java) //May be null


        if (mp != null) {
            if (java.lang.System.nanoTime() - mp.LastMessageTime < 1000L * 1000L * component.minTimeBetweenMessages.get()) { //0.1s by default
                cuser.sendMessage("§cYou are sending messages too quickly!")
                return true
            }
            mp.LastMessageTime = java.lang.System.nanoTime()
        }

        //DimensionManager.a()
        //IRegistry.ae
        //Bukkit.createWorld()
        //MinecraftServer.reload()
        //IRegistry
        //CraftServer
        ChatProcessing.doFunStuff(cuser, e, message)

        val channelidentifier = ChatProcessing.getChannelID(channel, e.origin)

        PluginMain.Companion.Instance.getServer().getConsoleSender()
            .sendMessage(kotlin.String.format("%s <%s§r> %s", channelidentifier, cuser.name, message))

        if (org.bukkit.Bukkit.getOnlinePlayers().size == 0) return false //Don't try to send to nobody (errors on 1.14)


        var colormode: TextColor? =
            net.kyori.adventure.text.format.NamedTextColor.NAMES.value(channel.color.get().getName())
        var colorModeChanged = false
        if (mp != null && mp.OtherColorMode != null) {
            colormode = net.kyori.adventure.text.format.NamedTextColor.NAMES.value(mp.OtherColorMode.getName())
            colorModeChanged = true
        }

        val formatters: java.util.ArrayList<MatchProviderBase?>
        if (component.allowFormatting.get()) {
            formatters =
                ChatProcessing.addFormatters(java.util.function.Predicate { sender: org.bukkit.entity.Player? ->
                    e.shouldSendTo(ChromaGamerBase.Companion.getFromSender(sender))
                }, component)
            if (colorModeChanged && mp.RainbowPresserColorMode) { // Only overwrite channel color
                ChatProcessing.createRPC(colormode, formatters)
            }
            ChatProcessing.pingedconsole = false // Will set it to true onmatch (static constructor)
        } else formatters = com.google.common.collect.Lists.newArrayList<MatchProviderBase>()

        val builder = ChatProcessing.createEmptyMessageLine(cuser, message, player, channelidentifier, e.origin)
        var combinetime = java.lang.System.nanoTime()
        ChatFormatter.Combine(formatters, message, builder, component.config, builder().color(colormode).build())
        combinetime = java.lang.System.nanoTime() - combinetime

        try {
            if (!channel.isGlobal) {
                val senderGroup: kotlin.String? = e.getGroupID(cuser)
                if (senderGroup == null) { // Never send messages if the group is null
                    cuser.sendMessage("§cYou don't have permission to send this message or something went wrong")
                    return true
                }
                val tc =
                    getIfEnabled<buttondevteam.chat.components.towny.TownyComponent?>(buttondevteam.chat.components.towny.TownyComponent::class.java)
                var spyConsumer: java.util.function.Consumer<org.bukkit.entity.Player?>? = null
                if (tc != null) spyConsumer = tc.handleSpiesInit(channel, builder)
                for (p in org.bukkit.Bukkit.getOnlinePlayers()) {
                    val group: kotlin.String?
                    if (player != null
                        && PluginMain.Companion.essentials.getUser(p)
                            .isIgnoredPlayer(PluginMain.Companion.essentials.getUser(player))
                    ) group = null // Don't send the message to them
                    else group = getGroupIfChatOn(p, e)
                    if (senderGroup == group) {
                        p.sendMessage(builder.build())
                        if (tc != null) spyConsumer!!.accept(p)
                    }
                    //Only sends if didn't send normally
                }
            } else for (p in org.bukkit.Bukkit.getOnlinePlayers()) p.sendMessage(builder.build())
        } catch (ex: java.lang.Exception) {
            SendException("An error occured while sending a chat message!", ex, PluginMain.Companion.Instance)
            cuser.sendMessage("§cAn error occured while sending the message.")
            return true
        }
        DebugCommand.Companion.SendDebugMessage(
            "-- Full ChatProcessing time: " + (java.lang.System.nanoTime() - processstart) / 1000000f + " ms"
        )
        DebugCommand.Companion.SendDebugMessage("-- ChatFormatter.Combine time: " + combinetime / 1000000f + " ms")
        return false
    }

    fun createRPC(colormode: TextColor?, formatters: java.util.ArrayList<MatchProviderBase?>) {
        val rpc = java.util.concurrent.atomic.AtomicInteger(0)
        formatters.add(
            RegexMatchProvider(
                "rpc",
                ChatProcessing.WORD_PATTERN,
                builder().color(colormode).onmatch({ match, cf, s ->
                    cf.setColor(ChatProcessing.RainbowPresserColors[rpc.getAndUpdate(java.util.function.IntUnaryOperator { i: kotlin.Int ->
                        var i = i
                        if (++i < ChatProcessing.RainbowPresserColors.size) i else 0
                    })])
                    match
                }).build()
            )
        )
    }

    fun createEmptyMessageLine(
        user: ChromaGamerBase, message: kotlin.String, @Nullable player: org.bukkit.entity.Player?,
        channelidentifier: kotlin.String, origin: kotlin.String
    ): net.kyori.adventure.text.TextComponent.Builder {
        val json = net.kyori.adventure.text.Component.text()
        ChatOnlyComponent.Companion.tellrawCreate(user.getAs<ChatPlayer?>(ChatPlayer::class.java), json)
        val channelHover = (if (ChatUtils.MCORIGIN == origin) "" else "From " + origin + "\n") + "Copy message"
        json.append(
            net.kyori.adventure.text.Component.text(channelidentifier)
                .hoverEvent(
                    net.kyori.adventure.text.event.HoverEvent.hoverEvent<net.kyori.adventure.text.Component>(
                        net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT,
                        text(channelHover).color(net.kyori.adventure.text.format.NamedTextColor.BLUE)
                    )
                ).clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand(message))
        )
        if (player != null) {
            if (PluginMain.Companion.permission.has(player, "tbmc.badge.diamond"))  // TODO: Cross-platform permissions
                json.append(
                    net.kyori.adventure.text.Component.text("[P]")
                        .color(net.kyori.adventure.text.format.NamedTextColor.AQUA).decorate(TextDecoration.BOLD)
                        .hoverEvent(
                            net.kyori.adventure.text.event.HoverEvent.hoverEvent<net.kyori.adventure.text.Component>(
                                net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT,
                                net.kyori.adventure.text.Component.text("Diamond Patreon supporter")
                            )
                        )
                )
            else if (PluginMain.Companion.permission.has(player, "tbmc.badge.gold")) json.append(
                net.kyori.adventure.text.Component.text("[P]")
                    .color(net.kyori.adventure.text.format.NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                    .hoverEvent(
                        net.kyori.adventure.text.event.HoverEvent.hoverEvent<net.kyori.adventure.text.Component>(
                            net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT,
                            net.kyori.adventure.text.Component.text("Gold Patreon supporter")
                        )
                    )
            )
        }
        json.append(net.kyori.adventure.text.Component.text(" <"))
        json.append(
            net.kyori.adventure.text.Component.text(user.name).hoverEvent(
                net.kyori.adventure.text.event.HoverEvent.hoverEvent<net.kyori.adventure.text.Component>(
                    net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT,
                    net.kyori.adventure.text.Component.text(user.getInfo(InfoTarget.MCHover))
                )
            )
        )
        json.append(net.kyori.adventure.text.Component.text("> "))
        return json
    }

    @JvmStatic
    fun getChannelID(channel: buttondevteam.core.component.channel.Channel, origin: kotlin.String): kotlin.String {
        return (("[" + (if (ChatUtils.MCORIGIN == origin) "" else "§8" + origin.get(0) + "§r|") + channel.displayName.get())
            + "]")
    }

    fun addFormatters(
        canSee: java.util.function.Predicate<org.bukkit.entity.Player?>,
        @Nullable component: FormatterComponent?
    ): java.util.ArrayList<MatchProviderBase?> {
        val formatters: java.util.ArrayList<MatchProviderBase?> =
            ChatProcessing.commonFormatters.clone() as java.util.ArrayList<MatchProviderBase?>

        val nottest: kotlin.Boolean //Not assigning a default value, so that it can only be used in the if
        if (((org.bukkit.Bukkit.getOnlinePlayers().size > 0).also { nottest = it }) || isTest) {
            val names: kotlin.Array<kotlin.String?>
            if (nottest) names = org.bukkit.Bukkit.getOnlinePlayers().stream().filter(canSee)
                .map<kotlin.String> { obj: org.bukkit.command.CommandSender? -> obj!!.getName() }
                .toArray<kotlin.String?> { _Dummy_.__Array__() }
            else {
                names = kotlin.arrayOfNulls<kotlin.String>(ChatProcessing.testPlayers.size)
                java.lang.System.arraycopy(ChatProcessing.testPlayers, 0, names, 0, ChatProcessing.testPlayers.size)
            }
            val nicknames = org.bukkit.Bukkit.getOnlinePlayers().stream().filter(canSee)
                .map<java.util.UUID> { obj: org.bukkit.entity.Player? -> obj!!.getUniqueId() }
                .map<kotlin.String?> { key: java.util.UUID? ->
                    buttondevteam.chat.listener.PlayerListener.Companion.nicknames.inverse().get(key)
                }
                .filter { obj: kotlin.String? -> java.util.Objects.nonNull(obj) }
                .toArray<kotlin.String?> { _Dummy_.__Array__() }

            val error = java.util.function.Consumer { message: kotlin.String? ->
                if (PluginMain.Companion.Instance != null) PluginMain.Companion.Instance.getLogger().warning(message)
                else kotlin.io.println(message)
            }

            if (names.size > 0)  //Add as first so it handles special characters (_) - though the order of the different types are defined
                formatters.add(
                    0,
                    StringMatchProvider(
                        "name", builder().color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                            .onmatch({ match, builder, section ->
                                val p: org.bukkit.entity.Player? = org.bukkit.Bukkit.getPlayer(match)
                                val pn: java.util.Optional<kotlin.String> = if (nottest)
                                    java.util.Optional.empty<kotlin.String>()
                                else
                                    java.util.Arrays.stream<kotlin.String?>(ChatProcessing.testPlayers)
                                        .filter { tp: kotlin.String? -> tp.equals(match, ignoreCase = true) }.findAny()
                                if (if (nottest) p == null else pn.isEmpty()) {
                                    error.accept("Error: Can't find player " + match + " but was reported as online.")
                                    return@onmatch "§c" + match + "§r"
                                }
                                val mpp: ChatPlayer = TBMCPlayerBase.Companion.getPlayer<ChatPlayer>(
                                    if (nottest) p!!.getUniqueId() else java.util.UUID(
                                        0,
                                        0
                                    ), ChatPlayer::class.java
                                )
                                if (nottest) {
                                    ChatProcessing.playPingSound(p!!, component)
                                }
                                val color = kotlin.String.format(
                                    "§%x",
                                    (if (mpp.GetFlairColor().toInt() == 0x00) 0xb else mpp.GetFlairColor())
                                )
                                color + (if (nottest) p!!.getName() else pn.get()) + "§r" //Fix name casing, except when testing
                            }).build(), true, names
                    )
                )

            if (nicknames.size > 0)  //Add as first so it handles special characters
                formatters.add(
                    0,
                    StringMatchProvider(
                        "nickname", builder().color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                            .onmatch({ match, builder, section ->
                                if (buttondevteam.chat.listener.PlayerListener.Companion.nicknames.containsKey(match.toLowerCase())) { //Made a stream and all that but I can actually store it lowercased
                                    val p: org.bukkit.entity.Player? = org.bukkit.Bukkit.getPlayer(
                                        buttondevteam.chat.listener.PlayerListener.Companion.nicknames.get(match.toLowerCase())
                                    )
                                    if (p == null) {
                                        error.accept(
                                            ("Error: Can't find player nicknamed "
                                                + match.toLowerCase() + " but was reported as online.")
                                        )
                                        return@onmatch "§c" + match + "§r"
                                    }
                                    ChatProcessing.playPingSound(p, component)
                                    return@onmatch PluginMain.Companion.essentials.getUser(p).getNickname()
                                }
                                error.accept(
                                    ("Player nicknamed " + match.toLowerCase()
                                        + " not found in nickname map but was reported as online.")
                                )
                                "§c" + match + "§r"
                            }).build(), true, nicknames
                    )
                )
        }
        return formatters
    }

    private fun playPingSound(p: org.bukkit.entity.Player, @Nullable component: FormatterComponent?) {
        if (component == null || component.notificationSound.get().length == 0) p.playSound(
            p.getLocation(),
            org.bukkit.Sound.ENTITY_ARROW_HIT_PLAYER,
            1.0f,
            0.5f
        ) // TODO: Airhorn
        else p.playSound(
            p.getLocation(), component.notificationSound.get(), 1.0f,
            component.notificationPitch.get()
        )
    }

    fun doFunStuff(user: ChromaGamerBase, event: TBMCChatEventBase, message: kotlin.String) {
        val fc = getIfEnabled<FunComponent?>(FunComponent::class.java)
        if (fc != null) fc.onChat(user, event, message)
    }
}
