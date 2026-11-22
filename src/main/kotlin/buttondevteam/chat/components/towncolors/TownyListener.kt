package buttondevteam.chat.components.towncolors

import buttondevteam.chat.PluginMain
import com.earth2me.essentials.User
import com.palmergames.bukkit.towny.event.*
import com.palmergames.bukkit.towny.`object`.Town
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.*

class TownyListener : Listener {
    @EventHandler
    fun onTownRename(event: RenameTownEvent) {
        val clrs = TownColorComponent.TownColors.remove(event.oldName.lowercase(Locale.getDefault()))
        if (clrs != null) TownColorComponent.TownColors[event.town.name.lowercase(Locale.getDefault())] = clrs
    }

    @EventHandler //Gets called on town load as well
    fun onTownJoin(event: TownAddResidentEvent) {
        val p = Bukkit.getPlayer(event.resident.name)
        if (p != null) TownColorComponent.updatePlayerColors(p)
    }

    @EventHandler
    fun onTownLeave(event: TownRemoveResidentEvent) {
        val p: Player? = Bukkit.getPlayer(event.resident.name)
        if (p != null) resetNameColor(p)
    }

    private fun resetNameColor(p: Player) {
        val user: User = PluginMain.essentials!!.getUser(p)
        user.setNickname(ChatColor.stripColor(user.getNick(true).replace("~", "")))
    }

    @EventHandler
    fun onTownDelete(event: DeleteTownEvent) {
        TownColorComponent.TownColors.remove(event.townName.lowercase(Locale.getDefault()))
    }

    @EventHandler
    fun onTownCreate(event: NewTownEvent) {
        val p: Player? = Bukkit.getPlayer(event.town.mayor.name)
        p?.sendMessage("§6Use /u towncolor to set a color for the town.")
    }

    //-----------------------------------------------------------------------------
    @EventHandler
    fun onNationRename(event: RenameNationEvent) {
        if (!TownColorComponent.component!!.useNationColors) return
        val clrs = TownColorComponent.NationColor.remove(event.oldName.lowercase(Locale.getDefault()))
        if (clrs != null) TownColorComponent.NationColor[event.nation.name.lowercase(Locale.getDefault())] = clrs
    }

    @EventHandler //Gets called on town load as well
    fun onNationJoin(event: NationAddTownEvent) {
        if (!TownColorComponent.component!!.useNationColors) return
        updateTownMembers(event.town)
    }

    @EventHandler
    fun onNationLeave(event: NationRemoveTownEvent) {
        if (!TownColorComponent.component!!.useNationColors) return
        TownyListener.updateTownMembers(event.town) //The town still has it's colours
    }

    @EventHandler
    fun onNationDelete(event: DeleteNationEvent) {
        if (!TownColorComponent.component!!.useNationColors) return
        TownColorComponent.NationColor.remove(event.nationName.lowercase(Locale.getDefault()))
    }

    @EventHandler
    fun onNationCreate(event: NewNationEvent) {
        if (!TownColorComponent.component!!.useNationColors) return
        val p = Bukkit.getPlayer(event.nation.capital.mayor.name)
        p?.sendMessage("§6Use /u nationcolor to set a color for the nation.")
    }

    companion object {
        fun updateTownMembers(town: Town) {
            town.getResidents().mapNotNull { r -> Bukkit.getPlayer(r.name) }
                .forEach { TownColorComponent.updatePlayerColors(it) }
        }
    }
}
