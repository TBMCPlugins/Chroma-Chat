package buttondevteam.chat.commands

import buttondevteam.lib.chat.Command2.*
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import org.bukkit.command.CommandSender
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

@CommandClass(
    modOnly = false, helpText = ["Minecraft Wiki linker" //
        , "Use without parameters to get a link to the wiki" //
        , "You can also search the wiki, for example:" //
        , " /mwiki beacon - Provides a link that redirects to the beacon's wiki page" //
    ]
)
class MWikiCommand : ICommand2MC() {
    @Subcommand
    fun def(sender: CommandSender, @OptionalArg @TextArg query: String?): Boolean {
        try {
            if (query == null) sender.sendMessage(
                *arrayOf<String>(
                    "§bMinecraft Wiki link: https://minecraft.wiki/",
                    "You can also search on it using /mwiki <query>"
                )
            )
            else sender.sendMessage(
                ("§bMinecraft Wiki link: https://minecraft.wiki/?search="
                    + URLEncoder.encode(query, "UTF-8") + "&title=Special%3ASearch&go=Go")
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return true
    }
}
