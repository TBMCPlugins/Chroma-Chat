package buttondevteam.thebuttonmcchat.commands.ucmds;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import buttondevteam.thebuttonmcchat.ChatFormatter;
import buttondevteam.thebuttonmcchat.PluginMain;
import buttondevteam.thebuttonmcchat.ChatPlayer;

public class CCommand extends UCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Rainbow mode ----",
				"This command allows you to talk in rainbow colors",
				"You need to be a donator or a mod to use this command" };
	}

	@Override
	public boolean OnCommand(CommandSender sender, String alias, String[] args) {
		Player player = (Player) sender;
		ChatPlayer p = ChatPlayer.AddPlayerIfNeeded(player
				.getUniqueId());
		if (args.length < 1) {
			if (PluginMain.permission.has(player, "tbmc.rainbow")) {
				p.RainbowPresserColorMode = !p.RainbowPresserColorMode;
				p.OtherColorMode = null;
				if (p.RainbowPresserColorMode)
					player.sendMessage("§eRainbow colors §aenabled.");
				else
					player.sendMessage("§eRainbow colors §cdisabled.");
			} else {
				player.sendMessage("§cYou don't have permission for this command. Donate to get it!");
				return true;
			}
		} else {
			if (PluginMain.permission.has(player, "tbmc.admin")) {
				p.RainbowPresserColorMode = false;
				p.OtherColorMode = null;
				try {
					p.OtherColorMode = ChatFormatter.Color.valueOf(args[0]
							.toLowerCase());
				} catch (Exception e) {
					player.sendMessage("§cUnknown message color: " + args[0]);
					player.sendMessage("§cUse color names, like blue, or dark_aqua");
				}
				if (p.OtherColorMode != null)
					player.sendMessage(String.format(
							"§eMessage color set to %s", p.OtherColorMode));
				else
					player.sendMessage("§eMessage color reset.");
			} else {
				player.sendMessage("§cYou don't have permission for this command.");
				return true;
			}
		}
		return true;
	}

	@Override
	public String GetUCommandPath() {
		return "c";
	}

}
