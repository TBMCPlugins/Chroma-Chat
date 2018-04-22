package buttondevteam.chat.commands.appendtext;

import buttondevteam.lib.chat.CommandClass;

@CommandClass(modOnly = false)
public class WaitWhatCommand extends AppendTextCommandBase {
	@Override
	public String[] GetHelpText(String alias) {
		return new String[] { //
				"§6--- Wait what ----", //
				"Wait what" //
		};
	}

	@Override
	public String GetAppendedText() {
		return "wait what";
	}
}
