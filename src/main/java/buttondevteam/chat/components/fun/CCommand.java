package buttondevteam.chat.components.fun;

import buttondevteam.chat.ChatPlayer;
import buttondevteam.chat.PluginMain;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import buttondevteam.lib.player.TBMCPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

@CommandClass(path = "u c", helpText = {
	"Rainbow mode",
	"This command allows you to talk in rainbow colors"
})
public class CCommand extends ICommand2MC {
	@Command2.Subcommand
	public boolean def(Player player, @Command2.OptionalArg String color) {
		ChatPlayer p = TBMCPlayer.getPlayer(player.getUniqueId(), ChatPlayer.class);
		if (color == null) {
			if (PluginMain.permission.has(player, "chroma.color.rainbow")) {
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
			if (PluginMain.permission.has(player, "chroma.color.custom")) {
				String x = color.toLowerCase();
				if ("off".equals(x)) {
					p.OtherColorMode = null;
					player.sendMessage("§eMessage color reset.");
					return true;
				}
				Optional<Color> oc = Arrays.stream(Color.values()).filter(c -> c.getName().equals(x)).findAny();
				if (!oc.isPresent()) {
					player.sendMessage("§cUnknown message color: " + color);
					player.sendMessage("§cUse color names, like blue, or dark_aqua");
					player.sendMessage("§cOr use 'off' to disable");
					return true;
				}
				p.RainbowPresserColorMode = false;
				p.OtherColorMode = oc.get();
				player.sendMessage(String.format("§eMessage color set to %s", p.OtherColorMode));
			} else {
				player.sendMessage("§cYou don't have permission for this command.");
				return true;
			}
		}
		return true;
	}
}
