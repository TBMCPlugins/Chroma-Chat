package buttondevteam.chat.components.formatter.formatting;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class FormattedSection {
	public int Start;
	public int End;
	public FormatSettings Settings;
	public List<String> Matches = new ArrayList<>();

	FormattedSection(FormatSettings settings, int start, int end, List<String> matches) {
		Start = start;
		End = end;
		Settings = FormatSettings.builder().build();
		Settings.copyFrom(settings);
		Matches.addAll(matches);
	}
}