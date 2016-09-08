package buttondevteam.chat.commands.ucmds.announce;

import buttondevteam.chat.commands.ucmds.UCommandBase;

public abstract class AnnounceCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias);

	@Override
	public String GetUCommandPath() {
		return "announce/" + GetAnnounceCommandPath();
	}

	public abstract String GetAnnounceCommandPath();

	@Override
	public boolean GetPlayerOnly() {
		return false;
	}
}
