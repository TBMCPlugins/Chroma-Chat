package buttondevteam.chat.commands.appendtext;

public final class TableflipCommand extends AppendTextCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Tableflip ----", //
				"This command appends a tableflip after your message", //
				"Or just makes you tableflip", //
				"Use either /" + alias + " <message> or just /" + alias };
	}

	@Override
	public String GetAppendedText() {
		return "(╯°□°）╯︵ ┻━┻";
	}
}
