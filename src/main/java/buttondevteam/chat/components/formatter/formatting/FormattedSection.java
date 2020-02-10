package buttondevteam.chat.components.formatter.formatting;

import java.util.ArrayList;
import java.util.List;

public class FormattedSection {
	public int Start;
	public int End;
	public FormatSettings Settings;
	public List<String> Matches = new ArrayList<String>();

	FormattedSection(FormatSettings settings, int start, int end, List<String> matches) {
		Start = start;
		End = end;
		Settings = settings;
		Matches.addAll(matches);
	}
}