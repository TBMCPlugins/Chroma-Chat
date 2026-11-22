package buttondevteam.chat

import buttondevteam.chat.components.flair.FlairStates
import buttondevteam.lib.player.PlayerClass
import buttondevteam.lib.player.TBMCPlayerBase
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

@PlayerClass(pluginname = "Chroma-Chat")
class ChatPlayer : TBMCPlayerBase() {
    val UserName by config.getData("UserName", "")

    val UserNames by config.getListData("UserNames", kotlin.collections.mutableListOf<String>())

    var FlairTime by config.getData<Int>("FlairTime", FlairTimeNone)

    val FlairState by config.getData<FlairStates>(
        "FlairState",
        FlairStates.NoComment,
        { fs -> FlairStates.valueOf((fs as String)) },
        java.util.function.Function { obj: FlairStates -> obj.toString() })

    var FCount by config.getData<Int>("FCount", 0)

    var FDeaths by config.getData<Int>("FDeaths", 0)

    var FlairCheater by config.getData<Boolean>("FlairCheater", false)

    val NameColorLocations by config.getListData<Int>(
        "NameColorLocations",
        kotlin.collections.mutableListOf<Int>()
    ) // No byte[], no TIntArrayList

    @Deprecated("")
    var Working: Boolean = false
    // public int Tables = 10;
    /**
     * Whether the player has enabled rainbow mode.
     */
    var RainbowPresserColorMode: Boolean = false

    /**
     * The custom chat text color that the player set.
     */
    var OtherColorMode: TextColor? = null
    var ChatOnly: Boolean = false
    var LastMessageTime: Long = 0L

    /**
     * Gets the player's flair, optionally formatting for Minecraft.
     *
     * @param noformats The MC formatting codes will be only applied if false
     * @return The flair
     */
    /**
     * Gets the player's flair, formatted for Minecraft.
     *
     * @return The flair
     */
    fun GetFormattedFlair(noformats: Boolean = false): Component {
        val time = FlairTime
        if (time == FlairTimeCantPress)
            return Component.text(if (noformats) "(can't press)" else "§r(--s)§r", NamedTextColor.WHITE)
        if (time == FlairTimeNonPresser)
            return Component.text(if (noformats) "(non-presser)" else "§7(--s)§r", NamedTextColor.GRAY)
        if (time == FlairTimeNone) return Component.text("")
        return Component.text("(${time}s)", GetFlairColor())
    }

    fun SetFlair(time: Int) {
        FlairTime = time
        FlairUpdate()
    }

    fun SetFlair(time: Int, cheater: Boolean) {
        FlairTime = time
        FlairCheater = cheater
        FlairUpdate()
    }

    fun FlairUpdate() {
        // Flairs from Command Block The Button - Teams
        // PluginMain.Instance.getServer().getScoreboardManager().getMainScoreboard().getTeams().add()

        val p = org.bukkit.Bukkit.getPlayer(uniqueId)
        p?.playerListName(p.displayName().append(GetFormattedFlair()))
    }

    fun GetFlairColor(): TextColor? {
        if (FlairCheater) return NamedTextColor.DARK_PURPLE
        val flairTime = FlairTime
        if (flairTime == FlairTimeNonPresser) return NamedTextColor.GRAY
        else if (flairTime == FlairTimeCantPress) return NamedTextColor.WHITE
        else if (flairTime in 52..60) return NamedTextColor.DARK_PURPLE
        else if (flairTime in 42..51) return NamedTextColor.BLUE
        else if (flairTime in 32..41) return NamedTextColor.GREEN
        else if (flairTime in 22..31) return NamedTextColor.YELLOW
        else if (flairTime in 11..21) return NamedTextColor.GOLD
        else if (flairTime in 0..11) return NamedTextColor.RED
        return null //Return null if none or too high, so names will get aqua default color, not white
    }

    val f: Double
        get() = FCount.toDouble() / FDeaths.toDouble()

    companion object {
        val FlairTimeNonPresser: Int = -1
        val FlairTimeCantPress: Int = -2
        val FlairTimeNone: Int = -3
    }
}
