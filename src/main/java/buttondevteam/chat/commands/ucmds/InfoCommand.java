package buttondevteam.chat.commands.ucmds;

import org.bukkit.command.CommandSender;

import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.ChromaGamerBase.InfoTarget;
import buttondevteam.lib.player.TBMCPlayerBase;

public class InfoCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { //
				"§6---- User information ----", //
				"Get some information known about the user.", //
				"Usage: /" + alias + " info <playername>" //
		};
	}

	@Override
	public String GetUCommandPath() {
		return "info";
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		if (args.length == 0)
			return false;
		if (args[0].equalsIgnoreCase("console") || args[0].equalsIgnoreCase("server")
				|| args[0].equalsIgnoreCase("@console")) {
			sender.sendMessage("The server console.");
			return true;
		}
		try (TBMCPlayer p = TBMCPlayerBase.getFromName(args[0], TBMCPlayer.class)) {
			if (p == null) {
				sender.sendMessage("§cThe specified player cannot be found");
				return true;
			}
			sender.sendMessage(p.getInfo(InfoTarget.MCCommand));
		} catch (Exception e) {
			TBMCCoreAPI.SendException("Error while getting player information!", e);
			sender.sendMessage("§cError while getting player information!");
		}
		return true;
	}

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}
}
