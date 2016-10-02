package buttondevteam.chat.formatting;

import java.util.ArrayList;
import java.util.Collection;

class FormattedSection {
	int Start;
	int End;
	ArrayList<ChatFormatter> Formatters = new ArrayList<ChatFormatter>();
	ArrayList<String> Matches = new ArrayList<String>();
	short RemCharFromStart;
	short RemCharFromEnd;
	ArrayList<Integer> RemCharPos = new ArrayList<Integer>();

	FormattedSection(ChatFormatter formatter, int start, int end, ArrayList<String> matches, short remcharfromstart,
			short remcharfromend, int remcharpos) {
		Start = start;
		End = end;
		Formatters.add(formatter);
		Matches.addAll(matches);
		RemCharFromStart = remcharfromstart;
		RemCharFromEnd = remcharfromend;
		RemCharPos.add(remcharpos);
	}

	FormattedSection(Collection<ChatFormatter> formatters, int start, int end, ArrayList<String> matches,
			short remcharfromstart, short remcharfromend, Collection<Integer> remcharpos) {
		Start = start;
		End = end;
		Formatters.addAll(formatters);
		Matches.addAll(matches);
		RemCharFromStart = remcharfromstart;
		RemCharFromEnd = remcharfromend;
		RemCharPos.addAll(remcharpos);
	}

	@Override
	public String toString() {
		return new StringBuilder("Section(").append(Start).append(", ").append(End).append(", formatters: ")
				.append(Formatters.toString()).append(", matches: ").append(Matches.toString()).append(", RemChars: ")
				.append(RemCharFromStart).append(", ").append(RemCharFromEnd).append(", ").append(RemCharPos)
				.append(")").toString();
	}
}