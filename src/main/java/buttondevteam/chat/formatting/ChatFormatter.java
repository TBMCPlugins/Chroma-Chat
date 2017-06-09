package buttondevteam.chat.formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import buttondevteam.chat.ChatProcessing;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.lib.chat.*;

public final class ChatFormatter {
	private Pattern regex;
	private boolean italic;
	private boolean bold;
	private boolean underlined;
	private boolean strikethrough;
	private boolean obfuscated;
	private Color color;
	private Function<String, String> onmatch;
	private String openlink;
	private Priority priority;
	private short removecharcount = 0;
	private boolean isrange;

	public ChatFormatter(Pattern regex, boolean italic, boolean bold, boolean underlined, boolean strikethrough,
			boolean obfuscated, Color color, Function<String, String> onmatch, String openlink, Priority priority,
			short removecharcount, boolean isrange) {
		super();
		this.regex = regex;
		this.italic = italic;
		this.bold = bold;
		this.underlined = underlined;
		this.strikethrough = strikethrough;
		this.obfuscated = obfuscated;
		this.color = color;
		this.onmatch = onmatch;
		this.openlink = openlink;
		if (priority == null)
			this.priority = Priority.Normal;
		else
			this.priority = priority;
		this.removecharcount = removecharcount;
		this.isrange = isrange;
	}

	public static void Combine(List<ChatFormatter> formatters, String str, TellrawPart tp) {
		/*
		 * This method assumes that there is always a global formatter
		 */
		ArrayList<FormattedSection> sections = new ArrayList<FormattedSection>();
		for (ChatFormatter formatter : formatters) {
			Matcher matcher = formatter.regex.matcher(str);
			while (matcher.find()) {
				DebugCommand.SendDebugMessage("Found match from " + matcher.start() + " to " + (matcher.end() - 1));
				DebugCommand.SendDebugMessage("With formatter:" + formatter);
				ArrayList<String> groups = new ArrayList<String>();
				for (int i = 0; i < matcher.groupCount(); i++)
					groups.add(matcher.group(i + 1));
				if (groups.size() > 0)
					DebugCommand.SendDebugMessage("First group: " + groups.get(0));
				FormattedSection section = new FormattedSection(formatter, matcher.start(), matcher.end() - 1, groups,
						formatter.removecharcount, formatter.removecharcount, formatter.isrange);
				sections.add(section);
			}
		}
		sections.sort((s1, s2) -> s1.Start == s2.Start
				? s1.End == s2.End ? Integer.compare(s2.Formatters.get(0).priority.GetValue(),
						s1.Formatters.get(0).priority.GetValue()) : Integer.compare(s2.End, s1.End)
				: Integer.compare(s1.Start, s2.Start));
		ArrayList<FormattedSection> combined = new ArrayList<>();
		Map<ChatFormatter, FormattedSection> nextSection = new HashMap<>();
		boolean escaped = false;
		int takenStart = -1, takenEnd = -1;
		ChatFormatter takenFormatter = null;
		for (int i = 0; i < sections.size(); i++) {
			// Set ending to -1 until closed with another 1 long "section" - only do this if IsRange is true
			final FormattedSection section = sections.get(i);
			if (!section.IsRange) {
				escaped = section.Formatters.contains(ChatProcessing.ESCAPE_FORMATTER) && !escaped; // Enable escaping on first \, disable on second
				if (escaped) // Don't add the escape character
					section.RemCharFromStart = 1;
				combined.add(section);
				DebugCommand.SendDebugMessage("Added " + (!escaped ? "not " : "") + "escaped section: " + section);
				continue;
			}
			if (!escaped) {
				if (combined.stream().anyMatch(s -> s.IsRange && (s.Start == section.Start
						|| (s.Start < section.Start ? s.End >= section.Start : s.Start <= section.End)))) {
					DebugCommand.SendDebugMessage("Range " + section + " overlaps with a combined section, ignoring.");
					continue;
				}
				if (section.Start == takenStart || (section.Start > takenStart && section.Start < takenEnd)) {
					/*
					 * if (nextSection.containsKey(section.Formatters.get(0)) ? section.RemCharFromStart <= takenEnd - takenStart : section.RemCharFromStart > takenEnd - takenStart) {
					 */
					if (section.RemCharFromStart < takenEnd - takenStart) {
						System.out.println("Lose: " + section);
						System.out.println("And win: " + takenFormatter);
						continue; // The current section loses
					}
					nextSection.remove(takenFormatter); // The current section wins
					System.out.println("Win: " + section);
					System.out.println("And lose: " + takenFormatter);
				}
				takenStart = section.Start;
				takenEnd = section.Start + section.RemCharFromStart;
				takenFormatter = section.Formatters.get(0);
				if (nextSection.containsKey(section.Formatters.get(0))) {
					FormattedSection s = nextSection.remove(section.Formatters.get(0));
					s.End = section.Start + section.RemCharFromStart - 1;
					// s.IsRange = false; // IsRange means it's a 1 long section indicating a start or an end
					combined.add(s);
					DebugCommand.SendDebugMessage("Finished section: " + s);
				} else {
					DebugCommand.SendDebugMessage("Adding next section: " + section);
					nextSection.put(section.Formatters.get(0), section);
				}
				DebugCommand
						.SendDebugMessage("New area taken: (" + takenStart + "-" + takenEnd + ") " + takenFormatter);
			} else {
				DebugCommand.SendDebugMessage("Skipping section: " + section);
				escaped = false; // Reset escaping if applied, like if we're at the '*' in '\*'
			}
		}
		sections = combined;
		boolean cont = true;
		boolean found = false;
		for (int i = 1; cont;) {
			int nextindex = i + 1;
			if (sections.size() < 2)
				break;
			DebugCommand.SendDebugMessage("i: " + i);
			FormattedSection firstSection = sections.get(i - 1);
			DebugCommand.SendDebugMessage("Combining sections " + firstSection + " and " + sections.get(i));
			if (firstSection.Start == sections.get(i).Start && firstSection.End == sections.get(i).End) {
				firstSection.Formatters.addAll(sections.get(i).Formatters);
				firstSection.Matches.addAll(sections.get(i).Matches);
				if (firstSection.RemCharFromStart < sections.get(i).RemCharFromStart)
					firstSection.RemCharFromStart = sections.get(i).RemCharFromStart;
				if (firstSection.RemCharFromEnd < sections.get(i).RemCharFromEnd)
					firstSection.RemCharFromEnd = sections.get(i).RemCharFromEnd;
				DebugCommand.SendDebugMessage("To section " + firstSection);
				sections.remove(i);
				found = true;
			} else if (firstSection.End > sections.get(i).Start && firstSection.Start < sections.get(i).End) {
				int origend = firstSection.End;
				firstSection.End = sections.get(i).Start - 1;
				int origend2 = sections.get(i).End;
				boolean switchends;
				if (switchends = origend2 < origend) {
					int tmp = origend;
					origend = origend2;
					origend2 = tmp;
				}
				FormattedSection section = new FormattedSection(firstSection.Formatters, sections.get(i).Start, origend,
						firstSection.Matches, sections.get(i).RemCharFromStart, firstSection.RemCharFromEnd, false);
				section.Formatters.addAll(sections.get(i).Formatters);
				section.Matches.addAll(sections.get(i).Matches); // TODO: Clean
				sections.add(i, section);
				nextindex++;
				FormattedSection thirdFormattedSection = sections.get(i + 1);
				if (switchends) { // Use the properties of the first section not the second one
					thirdFormattedSection.Formatters.clear();
					thirdFormattedSection.Formatters.addAll(firstSection.Formatters);
					thirdFormattedSection.Matches.clear();
					thirdFormattedSection.Matches.addAll(firstSection.Matches);
					short remchar = section.RemCharFromEnd;
					section.RemCharFromEnd = thirdFormattedSection.RemCharFromEnd;
					thirdFormattedSection.RemCharFromEnd = remchar;
				}
				firstSection.RemCharFromEnd = 0;
				thirdFormattedSection.RemCharFromStart = 0;
				thirdFormattedSection.Start = origend + 1;
				thirdFormattedSection.End = origend2;
				DebugCommand.SendDebugMessage("To sections 1:" + firstSection + "");
				DebugCommand.SendDebugMessage("  2:" + section + "");
				DebugCommand.SendDebugMessage("  3:" + thirdFormattedSection);
				found = true;
			}
			for (int j = i - 1; j <= i + 1; j++) {
				if (j < sections.size() && sections.get(j).End - sections.get(j).RemCharFromEnd < sections.get(j).Start
						+ sections.get(j).RemCharFromStart) {
					DebugCommand.SendDebugMessage("Removing section: " + sections.get(j));
					sections.remove(j);
					found = true;
				}
			}
			i = nextindex - 1;
			i++;
			if (i >= sections.size()) {
				if (found) {
					i = 1;
					found = false;
					sections.sort((s1, s2) -> s1.Start == s2.Start
							? s1.End == s2.End ? Integer.compare(s2.Formatters.get(0).priority.GetValue(),
									s1.Formatters.get(0).priority.GetValue()) : Integer.compare(s2.End, s1.End)
							: Integer.compare(s1.Start, s2.Start));
				} else
					cont = false;
			}
		}
		for (int i = 0; i < sections.size(); i++) {
			FormattedSection section = sections.get(i);
			DebugCommand.SendDebugMessage("Applying section: " + section);
			String originaltext;
			int start = section.Start + section.RemCharFromStart, end = section.End + 1 - section.RemCharFromEnd; // TODO: RemCharPos
			DebugCommand.SendDebugMessage("Start: " + start + " - End: " + end);
			StringBuilder textsb = new StringBuilder(str.substring(start, end));
			originaltext = textsb.toString();
			DebugCommand.SendDebugMessage("Section text: " + originaltext);
			Color color = null;
			boolean bold = false, italic = false, underlined = false, strikethrough = false, obfuscated = false;
			String openlink = null;
			section.Formatters.sort((cf2, cf1) -> cf1.priority.compareTo(cf2.priority));
			for (ChatFormatter formatter : section.Formatters) {
				DebugCommand.SendDebugMessage("Applying formatter: " + formatter);
				if (formatter.onmatch != null)
					originaltext = formatter.onmatch.apply(originaltext);
				if (formatter.color != null)
					color = formatter.color;
				if (formatter.bold)
					bold = true;
				if (formatter.italic)
					italic = true;
				if (formatter.underlined)
					underlined = true;
				if (formatter.strikethrough)
					strikethrough = true;
				if (formatter.obfuscated)
					obfuscated = true;
				if (formatter.openlink != null)
					openlink = formatter.openlink;
			}
			TellrawPart newtp = new TellrawPart("");
			newtp.setText(originaltext);
			if (color != null)
				newtp.setColor(color);
			newtp.setBold(bold);
			newtp.setItalic(italic);
			newtp.setUnderlined(underlined);
			newtp.setStrikethrough(strikethrough);
			newtp.setObfuscated(obfuscated);
			if (openlink != null && openlink.length() > 0) {
				newtp.setClickEvent(TellrawEvent.create(TellrawEvent.ClickAC, TellrawEvent.ClickAction.OPEN_URL,
						(section.Matches.size() > 0 ? openlink.replace("$1", section.Matches.get(0)) : openlink)))
						.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAC, TellrawEvent.HoverAction.SHOW_TEXT,
								new TellrawPart("Click to open").setColor(Color.Blue)));
			}
			tp.addExtra(newtp);
		}
	}

	@Override
	public String toString() {
		return new StringBuilder("F(").append(color).append(", ")
				.append((bold ? "bold" : "") + (italic ? "italic" : "") + (underlined ? "underlined" : "")
						+ (strikethrough ? "strikethrough" : "") + (obfuscated ? "obfuscated" : ""))
				.append(", ").append(openlink).append(", ").append(priority).append(", ").append(regex).append(")")
				.toString();
	}
}
