package buttondevteam.chat.commands.ucmds.admin;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.lib.chat.CommandClass;

@CommandClass(modOnly = true)
public abstract class AdminCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias);

}
