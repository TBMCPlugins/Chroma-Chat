package buttondevteam.chat.commands.ucmds;

import buttondevteam.chat.PluginMain;
import buttondevteam.lib.TBMCCoreAPI;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.player.ChromaGamerBase.InfoTarget;
import buttondevteam.lib.player.TBMCPlayer;
import buttondevteam.lib.player.TBMCPlayerBase;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@CommandClass(modOnly = false, helpText = {
	"User information", //
	"Get some information known about the user.", //
})
public class InfoCommand extends UCommandBase {
	@Command2.Subcommand
	public boolean def(CommandSender sender, OfflinePlayer player) {
		Bukkit.getScheduler().runTaskAsynchronously(PluginMain.Instance, () -> {
			try (TBMCPlayer p = TBMCPlayerBase.getPlayer(player.getUniqueId(), TBMCPlayer.class)) {
				if (p == null) {
					sender.sendMessage("§cThe specified player cannot be found");
					return;
				}
				sender.sendMessage(p.getInfo(InfoTarget.MCCommand));
			} catch (Exception e) {
				TBMCCoreAPI.SendException("Error while getting player information!", e);
				sender.sendMessage("§cError while getting player information!");
			}
		});
		return true;
	}
}
