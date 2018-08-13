package buttondevteam.chat.formatting;

import java.util.ArrayList;
import java.util.Collection;

class FormattedSection {
	int Start;
	int End;
	ArrayList<ChatFormatter> Formatters = new ArrayList<ChatFormatter>();
	ArrayList<String> Matches = new ArrayList<String>();
	ChatFormatter.Type type;

	FormattedSection(ChatFormatter formatter, int start, int end, ArrayList<String> matches, ChatFormatter.Type type) {
		Start = start;
		End = end;
		Formatters.add(formatter);
		Matches.addAll(matches);
		this.type = type;
	}

	FormattedSection(Collection<ChatFormatter> formatters, int start, int end, ArrayList<String> matches,
					 ChatFormatter.Type type) {
		Start = start;
		End = end;
		Formatters.addAll(formatters);
		Matches.addAll(matches);
		this.type = type;
	}

	@Override
	public String toString() {
		return "Section(" + Start + ", " + End + ", formatters: " +
				Formatters.toString() + ", matches: " + Matches.toString() + ", " +
				type + ")";
	}
}