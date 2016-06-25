package io.github.norbipeti.thebuttonmcchat.commands.ucmds.announce;

import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

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
