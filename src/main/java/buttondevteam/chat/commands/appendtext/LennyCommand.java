package buttondevteam.chat.commands.appendtext;

public final class LennyCommand extends AppendTextCommandBase {

	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { "§6---- Lenny ----", //
				"This command appends a Lenny face after your message", //
				"Or just sends ne", //
				"Use either /" + alias + " <message> or just /" + alias }; //
	}

	@Override
	public String GetAppendedText() {
		return "( ͡° ͜ʖ ͡°)";
	}

}
