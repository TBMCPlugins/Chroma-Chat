package io.github.norbipeti.thebuttonmcchat.commands.ucmds.announce;

import io.github.norbipeti.thebuttonmcchat.commands.ucmds.UCommandBase;

public abstract class AnnounceCommandBase extends UCommandBase {

	public abstract String[] GetHelpText(String alias);

	@Override
	public String GetUCommandPath() {
		return "announce";
	}

	public abstract String GetAnnounceCommandPath();

}
