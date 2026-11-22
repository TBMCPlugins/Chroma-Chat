package buttondevteam.chat.commands;

import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import org.bukkit.command.CommandSender;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@CommandClass(modOnly = false, helpText = {
	"Minecraft Wiki linker", //
	"Use without parameters to get a link to the wiki", //
	"You can also search the wiki, for example:", //
	" /mwiki beacon - Provides a link that redirects to the beacon's wiki page" //
})
public class MWikiCommand extends ICommand2MC {
	@Command2.Subcommand
	public boolean def(CommandSender sender, @Command2.OptionalArg @Command2.TextArg String query) {
		try {
			if (query == null)
				sender.sendMessage(new String[]{"§bMinecraft Wiki link: http://minecraft.gamepedia.com/",
					"You can also search on it using /mwiki <query>"});
			else
				sender.sendMessage("§bMinecraft Wiki link: http://minecraft.gamepedia.com/index.php?search="
					+ URLEncoder.encode(query, "UTF-8") + "&title=Special%3ASearch&go=Go");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return true;
	}

}
