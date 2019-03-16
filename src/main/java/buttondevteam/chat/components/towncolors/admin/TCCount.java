package buttondevteam.chat.components.towncolors.admin;

import buttondevteam.chat.commands.ucmds.admin.AdminCommandBase;
import buttondevteam.chat.components.towncolors.TownColorComponent;
import buttondevteam.lib.chat.Command2;
import buttondevteam.lib.chat.CommandClass;
import lombok.val;
import org.bukkit.command.CommandSender;

@CommandClass(helpText = {
	"Town Color Count", //
	"Sets how many colors can be used for a town." //
})
public class TCCount extends AdminCommandBase {
	@Command2.Subcommand
	public boolean def(CommandSender sender, byte count) {
		val comp = TownColorComponent.getComponent();
		comp.colorCount().set(count);
		sender.sendMessage("Color count set to " + count);
		return true;
	}
}
