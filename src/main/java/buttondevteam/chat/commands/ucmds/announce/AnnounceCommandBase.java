package buttondevteam.chat.commands.ucmds.announce;

import buttondevteam.chat.commands.ucmds.UCommandBase;
import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.OptionallyPlayerCommandClass;

@CommandClass(modOnly = true)
@OptionallyPlayerCommandClass(playerOnly = false)
public abstract class AnnounceCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias);

}
