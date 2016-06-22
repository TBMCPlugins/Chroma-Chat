package io.github.norbipeti.thebuttonmcchat.commands.appendtext;

public final class UnflipCommand extends AppendTextCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Unflip ----",
				"This command appends an unflip after your message",
				"Or just unflips as you",
				"Use either /" + alias + " <message> or just /" + alias };
	}

	@Override
	public String GetAppendedText() {
		return "┬─┬ ノ( ゜-゜ノ)";
	}

	@Override
	public String GetCommandName() {
		return "unflip";
	}

}
