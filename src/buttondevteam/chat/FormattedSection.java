package buttondevteam.thebuttonmcchat;

import java.util.ArrayList;
import java.util.Collection;

class FormattedSection {
	public int Start;
	public int End;
	public ArrayList<ChatFormatter> Formatters = new ArrayList<ChatFormatter>();
	public ArrayList<String> Matches = new ArrayList<String>();

	public FormattedSection(ChatFormatter formatter, int start, int end, ArrayList<String> matches) {
		Start = start;
		End = end;
		Formatters.add(formatter);
		Matches.addAll(matches);
	}

	public FormattedSection(Collection<ChatFormatter> formatters, int start, int end, ArrayList<String> matches) {
		Start = start;
		End = end;
		Formatters.addAll(formatters);
		Matches.addAll(matches);
	}

	@Override
	public String toString() {
		return new StringBuilder("Section(").append(Start).append(", ").append(End).append(", formatters: ")
				.append(Formatters.toString()).append(", matches: ").append(Matches.toString()).append(")")
				.toString();
	}
}