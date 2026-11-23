package buttondevteam.chat.components.towncolors

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.PluginMain
import buttondevteam.chat.components.towncolors.admin.NationColorCommand
import buttondevteam.chat.components.towncolors.admin.TCCount
import buttondevteam.chat.components.towncolors.admin.TownColorCommand
import buttondevteam.chat.components.towny.TownyComponent
import buttondevteam.core.ComponentManager
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.architecture.ComponentMetadata
import buttondevteam.lib.player.TBMCPlayerBase
import com.earth2me.essentials.User
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiFunction
import java.util.function.Consumer

/**
 * Town colors for Towny. It allows mayors and kings to set a color for their town/nation (nation can be disabled).
 * This color is applied to the player names in chat and on Dynmap, if used.
 */
@ComponentMetadata(depends = [TownyComponent::class])
class TownColorComponent : Component<PluginMain>(), Listener {
    /**
     * The amount of town colors allowed. If more than one is used (or nation colors are enabled), players can change how many letters to be in a specific color using /u ncolor.
     */
    var colorCount by config.getData(
        "colorCount", 1.toByte(),
        { cc: Any? -> (cc as Int).toByte() },
        { obj: Byte? -> obj!!.toInt() })

    /**
     * If enabled, players will have a nation-defined color in addition to town colors, white by default.
     * They can change how much of each color they want with this as well.
     */
    val useNationColors by config.getData("useNationColors", true)

    override fun enable() {
        component = this
        val loadTC = Consumer { cs: ConfigurationSection? ->
            TownColors.putAll(
                cs!!.getValues(true)
                    .mapValues {
                        (it.value as MutableList<*>)
                            .map { TextColor.fromHexString(it as String) ?: NamedTextColor.WHITE }.toTypedArray()
                    }
            )
        }
        val usenc = useNationColors
        val loadNC = Consumer { ncs: ConfigurationSection? ->
            NationColor.putAll(
                ncs!!.getValues(true).mapValues {
                    TextColor.fromHexString(it.value as String) ?: NamedTextColor.WHITE
                },
            )
        }
        val cs = config.config!!.getConfigurationSection("towncolors")
        if (cs != null) loadTC.accept(cs)
        if (usenc) {
            val ncs = config.config!!.getConfigurationSection("nationcolors")
            if (ncs != null) loadNC.accept(ncs)
        }

        TownColors.keys.removeIf { t: String? -> TownyComponent.dataSource!!.getTown(t) == null }  // Removes town colors for deleted/renamed towns
        if (usenc) NationColor.keys.removeIf { n: String? -> TownyComponent.dataSource!!.getNation(n) == null }  // Removes nation colors for deleted/renamed nations

        registerCommand(TownColorCommand())
        if (useNationColors) registerCommand(NationColorCommand())
        registerCommand(buttondevteam.chat.components.towncolors.TownColorCommand())
        if (useNationColors) registerCommand(buttondevteam.chat.components.towncolors.NationColorCommand())
        registerCommand(TCCount())
        registerCommand(NColorCommand())
        registerListener(TownyListener())
        registerListener(this)
    }

    override fun disable() {
        config.config!!.createSection("towncolors", TownColors.mapValues { it.value.map { it.asHexString() } })
        if (useNationColors) config.config!!.createSection(
            "nationcolors",
            NationColor.mapValues { it.value.asHexString() })
        config.signalChange()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        updatePlayerColors(
            event.getPlayer(),
            TBMCPlayerBase.getPlayer(event.getPlayer().uniqueId, ChatPlayer::class.java)
        )
    }

    companion object {
        /**
         * Names lowercased
         */
        val TownColors = mutableMapOf<String, Array<TextColor>>()

        /**
         * Names lowercased - nation color gets added to town colors when needed
         */
        val NationColor = mutableMapOf<String, TextColor>()

        var component: TownColorComponent? = null
            private set

        // TODO: Dynmap color update removed, might not even need it anymore, check official support

        private fun getPlayerNickname(player: Player, user: User, cp: ChatPlayer): String {
            var nickname = user.getNick(true)
            if (nickname.contains("~"))  //StartsWith doesn't work because of color codes
                nickname = nickname.replace("~", "") //It gets stacked otherwise

            val name: String = ChatColor.stripColor(nickname)!! //Enforce "town colors" on non-members
            val res = TownyComponent.dataSource!!.getResident(player.name)
            if (res == null || !res.hasTown()) return name
            try {
                val clrs = TownColors[res.town.name.lowercase(Locale.getDefault())]
                    ?: arrayOf(NamedTextColor.WHITE)//Use white as default town color
                val ret = StringBuilder()
                val prevlen = AtomicInteger()
                //Len==0 if last part
                // 'Odds' are the last character is chopped off so we make sure to include all chars at the end
                val anyColoredNamePart = BiFunction { c: TextColor, len: Int ->
                    net.kyori.adventure.text.Component.text(
                        if (len == 0) name.substring(prevlen.get())
                        else name.substring(prevlen.get(), prevlen.addAndGet(len)),
                        c
                    )
                }
                val coloredNamePart = BiFunction { len: Int, i: Int ->
                    anyColoredNamePart.apply(
                        clrs[i],
                        if (i + 1 == clrs.size) 0 else len
                    )
                }
                val len = name.length / (clrs.size + 1) //The above param is needed because this isn't always passed
                val usenc: Boolean = component!!.useNationColors
                val nclar = cp.NameColorLocations
                var ncl: IntArray? = nclar.toIntArray()
                if (ncl == null || ncl.sum() != name.length || ncl.size != clrs.size + (if (usenc) 1 else 0))  //+1: Nation color
                    ncl = null // Reset if name length changed

                if (usenc) {
                    val nc = (if (res.town.hasNation()) NationColor[res.town.nation.name.lowercase(Locale.getDefault())]
                    else null) ?: NamedTextColor.WHITE
                    ret.append(
                        anyColoredNamePart.apply(
                            nc,
                            if (ncl == null) len else ncl[0]
                        )
                    ) //Make first color the nation color
                }
                for (i in clrs.indices) ret.append(
                    coloredNamePart.apply(
                        if (ncl == null) len else (if (usenc) ncl[i + 1] else ncl[i]),
                        i
                    )
                )
                return ret.toString()
            } catch (_: NotRegisteredException) {
                return nickname
            }
        }

        /**
         * Checks if the component is enabled
         */
        /**
         * Checks if the component is enabled
         */
        fun updatePlayerColors(
            player: Player,
            cp: ChatPlayer = TBMCPlayerBase.getPlayer(player.uniqueId, ChatPlayer::class.java)
        ) { //Probably at join - nop, nicknames
            if (!ComponentManager.isEnabled(TownColorComponent::class.java)) return
            val user: User = PluginMain.essentials!!.getUser(player)
            user.setNickname(getPlayerNickname(player, user, cp))
            user.setDisplayNick() //These won't fire the nick change event
            cp.FlairUpdate() //Update in list
        }
    }
}
