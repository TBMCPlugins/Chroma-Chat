package buttondevteam.chat.commands;

import org.bukkit.command.CommandSender;

import buttondevteam.lib.chat.TBMCCommandBase;

public class MWikiCommand extends TBMCCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] {
				"§6---- Minecraft Wiki linker ----",
				"Use without parameters to get a link to the wiki",
				"You can also search the wiki, for example:",
				" /"
						+ alias
						+ " beacon - Provides a link that redirects to the beacon's wiki page" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		String query = "";
		for (int i = 0; i < args.length; i++)
			query += args[i] + " ";
		query = query.trim();
		if (args.length == 0)
			sender.sendMessage("§bMinecraft Wiki link: http://minecraft.gamepedia.com/");
		else
			sender.sendMessage("§bMinecraft Wiki link: http://minecraft.gamepedia.com/index.php?search="
					+ query + "&title=Special%3ASearch&go=Go");
		return true;
	}

	@Override
	public String GetCommandPath() {
		return "mwiki";
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}

	@Override
	public boolean GetModOnly() {
		return false;
	}

}
