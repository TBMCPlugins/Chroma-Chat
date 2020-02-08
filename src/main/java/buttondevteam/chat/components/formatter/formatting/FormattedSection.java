package buttondevteam.chat.components.formatter.formatting;

import java.util.ArrayList;

public class FormattedSection {
	public int Start;
	public int End;
	public FormatSettings Settings;
	public ArrayList<String> Matches = new ArrayList<String>();

	FormattedSection(FormatSettings settings, int start, int end, ArrayList<String> matches) {
		Start = start;
		End = end;
		Settings = settings;
		Matches.addAll(matches);
	}
}