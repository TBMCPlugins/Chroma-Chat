package buttondevteam.chat.commands.ucmds;

import buttondevteam.lib.chat.CommandClass;
import buttondevteam.lib.chat.ICommand2MC;
import buttondevteam.lib.chat.OptionallyPlayerCommandClass;

@CommandClass(modOnly = false, path = "u")
@OptionallyPlayerCommandClass(playerOnly = false)
public abstract class UCommandBase extends ICommand2MC {
}
