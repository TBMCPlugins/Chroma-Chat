package buttondevteam.chat.components.towncolors

import buttondevteam.chat.ChatPlayer
import buttondevteam.chat.commands.ucmds.UCommandBase
import buttondevteam.chat.components.towny.TownyComponent
import buttondevteam.lib.ChromaUtils.asString
import buttondevteam.lib.architecture.config.ConfigList
import buttondevteam.lib.chat.Command2
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.player.TBMCPlayerBase
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

@CommandClass(
    helpText = [
        "Name color",
        "This command allows you to set how the town colors look on your name.",
        "To use this command, you need to be in a town which has town colors set.",
        "Use a vertical line (or a colon) as a separator between the colors.",
        "Example: /u ncolor Norbi|Peti --> §6Norbi§ePeti"
    ]
)
class NColorCommand : UCommandBase() {
    @Command2.Subcommand
    fun def(player: Player, nameWithLines: String): Boolean {
        val res: Resident? = TownyComponent.dataSource!!.getResident(player.name)
        val town: Town? = res?.let { if (it.hasTown()) it.town else null }
        try {
            if (town == null) {
                player.sendMessage("§cYou need to be in a town.")
                return true
            }
        } catch (e: Exception) {
            player.sendMessage("§cYou need to be in a town. ($e)")
            return true
        }
        val name = player.displayName().asString().replace("~", "") //Remove ~
        //Don't add ~ for nicknames
        if (!nameWithLines.replace("|", "").replace(":", "").equals(name, ignoreCase = true)) {
            player.sendMessage(
                ("§cThe name you gave doesn't match your name. Make sure to use "
                    + name + "§c with added vertical lines (|) or colons (:).")
            )
            return true
        }
        val nameparts = nameWithLines.split("[|:]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val towncolors: Array<TextColor>? =
            TownColorComponent.TownColors[town.name.lowercase(java.util.Locale.getDefault())]
        if (towncolors == null) {
            player.sendMessage("§cYour town doesn't have a color set. The town mayor can set it using /u towncolor.")
            return true
        }
        val component = TownColorComponent.component!!
        val nationColors = (if (component.useNationColors) 1 else 0).toByte()
        if (nameparts.size < towncolors.size + nationColors) { //+1: Nation color
            player.sendMessage("§cYou need more vertical lines (|) or colons (:) in your name. (Should have " + (towncolors.size - 1 + 1) + ")") //Nation color
            return true
        }
        if (nameparts.size > (towncolors.size + nationColors) * 2) {
            player.sendMessage("§cYou have waay too many vertical lines (|) or colons (:) in your name. (Should have " + (towncolors.size - 1 + 1) + ")")
            return true
        }
        if (nameparts.size > towncolors.size + nationColors) {
            player.sendMessage("§cYou have too many vertical lines (|) or colons (:) in your name. (Should have " + (towncolors.size - 1 + 1) + ")")
            return true
        }
        val cp = TBMCPlayerBase.getPlayer(player.uniqueId, ChatPlayer::class.java)
        val list = nameparts.map { it.length }
        if (list.contains(0)) {
            player.sendMessage("§cAll colors need to be represented in your name. (Use as Test|name|123 instead of |Testname123|)")
            return true
        }
        val clist: ConfigList<Int> = cp.NameColorLocations // No byte[], no TIntArrayList
        clist.clear()
        clist.addAll(list)
        TownColorComponent.updatePlayerColors(player, cp)
        player.sendMessage("§bName colors set: " + player.displayName)
        return true
    }
}
