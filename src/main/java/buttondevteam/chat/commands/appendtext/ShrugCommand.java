package buttondevteam.chat.commands.appendtext;

public final class ShrugCommand extends AppendTextCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Shrug ----",
				"This command appends a shrug after your message",
				"Or just makes you shrug",
				"Use either /" + alias + " <message> or just /" + alias };
	}

	@Override
	public String GetAppendedText() {
		return "¯\\\\\\_(ツ)\\_/¯";
	}

	@Override
	public String GetCommandPath() {
		return "shrug";
	}

}
