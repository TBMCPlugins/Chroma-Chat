package buttondevteam.chat.components.fun;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.*;
import buttondevteam.lib.player.TBMCPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;

@CommandClass(path = "u c", helpText = {
	"Rainbow mode",
	"This command allows you to talk in rainbow colors"
})
@OptionallyPlayerCommandClass(playerOnly = true)
public class CCommand extends ICommand2MC {
	@Command2.Subcommand
	public boolean def(Player player, @Command2.OptionalArg String color) {
		ChatPlayer p = TBMCPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class);
		if (color == null) {
			if (PluginMain.permission.has(player, "thorpe.color.rainbow")) {
				p.RainbowPresserColorMode = !p.RainbowPresserColorMode;
				p.OtherColorMode = null;
				if (p.RainbowPresserColorMode)
					player.sendMessage("§eRainbow colors §aenabled.");
				else
					player.sendMessage("§eRainbow colors §cdisabled.");
			} else {
				player.sendMessage("§cYou don't have permission for this command.");
				return true;
			}
		} else {
			if (PluginMain.permission.has(player, "thorpe.color.custom")) {
				p.RainbowPresserColorMode = false;
				p.OtherColorMode = null;
				try {
					String x = color.toLowerCase();
					p.OtherColorMode = Arrays.stream(Color.values()).filter(c -> c.getName().equals(x)).findAny().orElse(null);
				} catch (Exception e) {
					player.sendMessage("§cUnknown message color: " + color);
					player.sendMessage("§cUse color names, like blue, or dark_aqua");
				}
				if (p.OtherColorMode != null)
					player.sendMessage(String.format("§eMessage color set to %s", p.OtherColorMode));
				else
					player.sendMessage("§eMessage color reset.");
			} else {
				player.sendMessage("§cYou don't have permission for this command.");
				return true;
			}
		}
		return true;
	}
}
