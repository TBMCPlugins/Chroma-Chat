package buttondevteam.chat.components.towny

import buttondevteam.chat.PluginMain
import buttondevteam.core.component.channel.Channel
import buttondevteam.lib.architecture.Component
import buttondevteam.lib.chat.TBMCChatAPI.registerChatChannel
import buttondevteam.lib.player.ChromaGamerBase
import buttondevteam.lib.player.TBMCPlayerBase
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownyUniverse
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException
import com.palmergames.bukkit.towny.`object`.Nation
import com.palmergames.bukkit.towny.`object`.Resident
import com.palmergames.bukkit.towny.`object`.Town
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.function.Consumer
import java.util.function.Function

/**
 * This component manages the town and nation chat. It's also needed for the TownColorComponent.
 * It provides the TC and NC channels, and posts Towny messages (global, town, nation) to the correct channels for other platforms like Discord.
 * You can disable /tc and /nc in Chroma-Core's config if you only want to use the TownColorComponent.
 */
class TownyComponent : Component<PluginMain>() {
    private var TownChat: Channel? = null
    private var NationChat: Channel? = null

    override fun enable() {
        dataSource = TownyAPI.getInstance()
        // Creates a snapshot of towns, new towns will be added when needed
        Towns = TownyUniverse.getInstance().towns.map { it.name }.toList()
        // Same here but with nations
        Nations = TownyUniverse.getInstance().nations.map { obj -> obj.name }.toList()
        registerChatChannel(
            Channel(
                "TC",
                NamedTextColor.DARK_AQUA,
                "tc",
                Function { s: ChromaGamerBase? -> checkTownNationChat(s, false) }).also { TownChat = it })
        registerChatChannel(
            Channel(
                "§6NC§f",
                NamedTextColor.GOLD,
                "nc",
                Function { s: ChromaGamerBase? -> checkTownNationChat(s, true) }).also { NationChat = it })
        TownyAnnouncer.setup(TownChat, NationChat)
    }

    override fun disable() {
        TownyAnnouncer.setdown()
    }

    fun handleSpiesInit(channel: Channel, json: TextComponent.Builder): Consumer<Player?> {
        if (channel.identifier == TownChat!!.identifier || channel.identifier == NationChat!!.identifier) {
            // TODO: Cannot prepend to json, so we need to run this ealier
            //((List<TellrawPart>) json.getExtra()).add(0, new TellrawPart("[SPY]"));
            return Consumer { p: Player? -> handleSpies(channel, p!!, json) }
        }
        return Consumer { p: Player? -> }
    }

    private fun handleSpies(channel: Channel, p: Player, jsonstr: TextComponent.Builder) {
        if (channel.identifier == TownChat!!.identifier || channel.identifier == NationChat!!.identifier) {
            val res = dataSource!!.getResident(p.name) ?: return
            if (res.hasMode("spy")) p.sendMessage(jsonstr.build())
        }
    }

    companion object {
        var dataSource: TownyAPI? = null
        private var Towns: List<String>? = null
        private var Nations: List<String>? = null

        /**
         * Return the error message for the message sender if they can't send it and the score
         */
        private fun checkTownNationChat(user: ChromaGamerBase?, nationchat: Boolean): Channel.RecipientTestResult {
            if (user !is TBMCPlayerBase) return Channel.RecipientTestResult("You are not a player!")
            val sender = user.offlinePlayer
            val resident = dataSource!!.getResident(sender.name)
            var result = checkTownNationChatInternal(nationchat, resident)
            if (result.errormessage != null && resident != null && resident.getModes()
                    .contains("spy")
            )  // Only use spy if they wouldn't see it
                result = Channel.RecipientTestResult(
                    1000,
                    "allspies"
                ) // There won't be more than a thousand towns/nations probably

            return result
        }

        private fun checkTownNationChatInternal(
            nationchat: Boolean,
            resident: Resident?
        ): Channel.RecipientTestResult {
            try {
                var town: Town? = null
                if (resident != null && resident.hasTown()) town = resident.getTown()
                if (town == null) return Channel.RecipientTestResult("You aren't in a town.")
                var nation: Nation? = null
                val index: Int
                if (nationchat) {
                    if (town.hasNation()) nation = town.nation
                    if (nation == null) return Channel.RecipientTestResult("Your town isn't in a nation.")
                    index = getTownNationIndex(nation.name, true)
                } else index = getTownNationIndex(town.name, false)
                return Channel.RecipientTestResult(index, if (nationchat) nation!!.name else town.name)
            } catch (e: NotRegisteredException) {
                return Channel.RecipientTestResult("You (probably) aren't knwon by Towny! (Not in a town)")
            }
        }

        fun getTownNationIndex(name: String, nation: Boolean): Int {
            var list = if (nation) Nations else Towns
            var index: Int = list!!.indexOf(name)
            if (index < 0) {
                list += name
                index = list.size - 1
            }
            return index
        }
    }
}
