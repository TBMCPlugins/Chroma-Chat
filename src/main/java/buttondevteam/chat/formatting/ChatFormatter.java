package buttondevteam.chat.formatting;

import buttondevteam.chat.ChatProcessing;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.lib.chat.Color;
import buttondevteam.lib.chat.Priority;
import lombok.Builder;
import lombok.Data;
import lombok.val;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A {@link ChatFormatter} shows what formatting to use based on regular expressions. {@link ChatFormatter#Combine(List, String, TellrawPart)} is used to turn it into a {@link TellrawPart}, combining
 * intersecting parts found, for example when {@code _abc*def*ghi_} is said in chat, it'll turn it into an underlined part, then an underlined <i>and italics</i> part, finally an underlined part
 * again.
 * 
 * @author NorbiPeti
 *
 */
@Data
@Builder
public final class ChatFormatter {
	Pattern regex;
	boolean italic;
	boolean bold;
	boolean underlined;
	boolean strikethrough;
	boolean obfuscated;
	Color color;
	BiFunction<String, ChatFormatter, String> onmatch;
	String openlink;
	@Builder.Default
	Priority priority = Priority.Normal;
	@Builder.Default
	short removeCharCount = 0;
	@Builder.Default
    Type type = Type.Normal;

    public enum Type {
        Normal,
        /**
         * Matches a start and an end section which gets converted to one section (for example see italics)
         */
        Range,
        /**
         * Exclude matching area from further processing (besides this formatter)
         */
        Excluder
    }

	public static void Combine(List<ChatFormatter> formatters, String str, TellrawPart tp) {
		/*
		 * This method assumes that there is always a global formatter
         */
        header("ChatFormatter.Combine begin"); //TODO: Handle excluder formatters first
		ArrayList<FormattedSection> sections = new ArrayList<FormattedSection>();
		for (ChatFormatter formatter : formatters) {
			Matcher matcher = formatter.regex.matcher(str);
			while (matcher.find()) {
				DebugCommand.SendDebugMessage("Found match from " + matcher.start() + " to " + (matcher.end() - 1));
				DebugCommand.SendDebugMessage("With formatter:" + formatter);
				sendMessageWithPointer(str, matcher.start(), matcher.end() - 1);
				ArrayList<String> groups = new ArrayList<String>();
				for (int i = 0; i < matcher.groupCount(); i++)
					groups.add(matcher.group(i + 1));
				if (groups.size() > 0)
					DebugCommand.SendDebugMessage("First group: " + groups.get(0));
				FormattedSection section = new FormattedSection(formatter, matcher.start(), matcher.end() - 1, groups,
						formatter.type);
				sections.add(section);
			}
		}
		sections.sort(
				(s1, s2) -> s1.Start == s2.Start
						? s1.End == s2.End ? Integer.compare(s2.Formatters.get(0).priority.GetValue(),
								s1.Formatters.get(0).priority.GetValue()) : Integer.compare(s2.End, s1.End)
						: Integer.compare(s1.Start, s2.Start));

		/**
		 * 0: Start - 1: End index
		 */
		val remchars = new ArrayList<int[]>();

		header("Range section conversion");
		ArrayList<FormattedSection> combined = new ArrayList<>();
		Map<ChatFormatter, FormattedSection> nextSection = new HashMap<>();
		boolean escaped = false;
		int takenStart = -1, takenEnd = -1;
		ChatFormatter takenFormatter = null;
		for (int i = 0; i < sections.size(); i++) {
			// Set ending to -1 until closed with another 1 long "section" - only do this if IsRange is true
			final FormattedSection section = sections.get(i);
            if (section.type!=Type.Range) {
				escaped = section.Formatters.contains(ChatProcessing.ESCAPE_FORMATTER) && !escaped; // Enable escaping on first \, disable on second
				if (escaped) // Don't add the escape character
					remchars.add(new int[]{section.Start, section.Start});
				combined.add(section); // This will delete the \
				DebugCommand.SendDebugMessage("Added " + (!escaped ? "not " : "") + "escaper section: " + section);
				sendMessageWithPointer(str, section.Start, section.End);
				continue;
            }
            if (!escaped) {
                if (combined.stream().anyMatch(s -> section.type != Type.Range && (s.Start == section.Start
						|| (s.Start < section.Start ? s.End >= section.Start : s.Start <= section.End)))) {
					DebugCommand.SendDebugMessage("Range " + section + " overlaps with a combined section, ignoring.");
					sendMessageWithPointer(str, section.Start, section.End);
					continue;
				}
				if (section.Start == takenStart || (section.Start > takenStart && section.Start < takenEnd)) {
					/*
					 * if (nextSection.containsKey(section.Formatters.get(0)) ? section.RemCharFromStart <= takenEnd - takenStart : section.RemCharFromStart > takenEnd - takenStart) {
					 */
					if (section.Formatters.get(0).removeCharCount < takenEnd - takenStart) {
						DebugCommand.SendDebugMessage("Lose: " + section);
						sendMessageWithPointer(str, section.Start, section.End);
						DebugCommand.SendDebugMessage("And win: " + takenFormatter);
						continue; // The current section loses
					}
					nextSection.remove(takenFormatter); // The current section wins
					DebugCommand.SendDebugMessage("Win: " + section);
					sendMessageWithPointer(str, section.Start, section.End);
					DebugCommand.SendDebugMessage("And lose: " + takenFormatter);
				}
				takenStart = section.Start;
				takenEnd = section.Start + section.Formatters.get(0).removeCharCount;
				takenFormatter = section.Formatters.get(0);
				if (nextSection.containsKey(section.Formatters.get(0))) {
					FormattedSection s = nextSection.remove(section.Formatters.get(0));
					// section: the ending marker section - s: the to-be full section
					s.End = takenEnd - 1; //Take the remCharCount into account as well
					// s.IsRange = false; // IsRange means it's a 1 long section indicating a start or an end
					combined.add(s);
					DebugCommand.SendDebugMessage("Finished section: " + s);
					sendMessageWithPointer(str, s.Start, s.End);
				} else {
					DebugCommand.SendDebugMessage("Adding next section: " + section);
					sendMessageWithPointer(str, section.Start, section.End);
					nextSection.put(section.Formatters.get(0), section);
				}
				DebugCommand
						.SendDebugMessage("New area taken: (" + takenStart + "-" + takenEnd + ") " + takenFormatter);
				sendMessageWithPointer(str, takenStart, takenEnd);
			} else {
				DebugCommand.SendDebugMessage("Skipping section: " + section); // This will keep the text (character)
				sendMessageWithPointer(str, section.Start, section.End);
				escaped = false; // Reset escaping if applied, like if we're at the '*' in '\*'
			}
		}
		for (val sec : nextSection.values()) {
			sec.End = str.length() - 1;
			combined.add(sec);
			DebugCommand.SendDebugMessage("Finished unfinished section: " + sec);
			sendMessageWithPointer(str, sec.Start, sec.End);
		}
		sections = combined;

		header("Adding remove chars (RC)"); // Important to add after the range section conversion
		sections.stream()
				.flatMap(fs -> fs.Formatters.stream().filter(cf -> cf.removeCharCount > 0)
						.mapToInt(cf -> cf.removeCharCount).mapToObj(rcc -> new int[]{fs.Start, fs.Start + rcc - 1}))
				.forEach(rc -> remchars.add(rc));
		sections.stream()
				.flatMap(fs -> fs.Formatters.stream().filter(cf -> cf.removeCharCount > 0)
						.mapToInt(cf -> cf.removeCharCount).mapToObj(rcc -> new int[]{fs.End - rcc + 1, fs.End}))
				.forEach(rc -> remchars.add(rc));
		DebugCommand.SendDebugMessage("Added remchars:");
		DebugCommand
				.SendDebugMessage(remchars.stream().map(rc -> Arrays.toString(rc)).collect(Collectors.joining("; ")));

		header("Section combining");
		boolean cont = true;
		boolean found = false;
		for (int i = 1; cont;) {
			int nextindex = i + 1;
			if (sections.size() < 2)
				break;
			DebugCommand.SendDebugMessage("i: " + i);
			FormattedSection firstSection = sections.get(i - 1);
			DebugCommand.SendDebugMessage("Combining sections " + firstSection);
			sendMessageWithPointer(str, firstSection.Start, firstSection.End);
			DebugCommand.SendDebugMessage(" and " + sections.get(i));
			sendMessageWithPointer(str, sections.get(i).Start, sections.get(i).End);
			if (firstSection.Start == sections.get(i).Start && firstSection.End == sections.get(i).End) {
				firstSection.Formatters.addAll(sections.get(i).Formatters);
				firstSection.Matches.addAll(sections.get(i).Matches);
				DebugCommand.SendDebugMessage("To section " + firstSection);
				sendMessageWithPointer(str, firstSection.Start, firstSection.End);
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
                        firstSection.Matches, Type.Normal);
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
				}
				thirdFormattedSection.Start = origend + 1;
				thirdFormattedSection.End = origend2;

				ArrayList<FormattedSection> sts = sections;
				Predicate<FormattedSection> removeIfNeeded = s -> {
					if (s.Start < 0 || s.End < 0 || s.Start > s.End) {
						DebugCommand.SendDebugMessage("Removing section: " + s);
						sendMessageWithPointer(str, s.Start, s.End);
						sts.remove(s);
						return true;
					}
					return false;
				};

				DebugCommand.SendDebugMessage("To sections");
				if (!removeIfNeeded.test(firstSection)) {
					DebugCommand.SendDebugMessage("  1:" + firstSection + "");
					sendMessageWithPointer(str, firstSection.Start, firstSection.End);
				}
				if (!removeIfNeeded.test(section)) {
					DebugCommand.SendDebugMessage("  2:" + section + "");
					sendMessageWithPointer(str, section.Start, section.End);
				}
				if (!removeIfNeeded.test(thirdFormattedSection)) {
					DebugCommand.SendDebugMessage("  3:" + thirdFormattedSection);
					sendMessageWithPointer(str, thirdFormattedSection.Start, thirdFormattedSection.End);
				}
				found = true;
			}
			for (int j = i - 1; j <= i + 1; j++) {
				if (j < sections.size() && sections.get(j).End < sections.get(j).Start) {
					DebugCommand.SendDebugMessage("Removing section: " + sections.get(j));
					sendMessageWithPointer(str, sections.get(j).Start, sections.get(j).End);
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
					sections.sort(
							(s1, s2) -> s1.Start == s2.Start
									? s1.End == s2.End
											? Integer.compare(s2.Formatters.get(0).priority.GetValue(),
													s1.Formatters.get(0).priority.GetValue())
											: Integer.compare(s2.End, s1.End)
									: Integer.compare(s1.Start, s2.Start));
				} else
					cont = false;
			}
		}

		header("Section applying");
		for (int i = 0; i < sections.size(); i++) {
			FormattedSection section = sections.get(i);
			DebugCommand.SendDebugMessage("Applying section: " + section);
			String originaltext;
			int start = section.Start, end = section.End;
			DebugCommand.SendDebugMessage("Start: " + start + " - End: " + end);
			sendMessageWithPointer(str, start, end);
			val rcs = remchars.stream().filter(rc -> rc[0] <= start && start <= rc[1]).findAny();
			val rce = remchars.stream().filter(rc -> rc[0] <= end && end <= rc[1]).findAny();
			int s = start, e = end;
			if (rcs.isPresent())
				s = rcs.get()[1] + 1;
			if (rce.isPresent())
				e = rce.get()[0] - 1;
			DebugCommand.SendDebugMessage("After RC - Start: " + s + " - End: " + e);
            if (e - s < 0) { //e-s==0 means the end char is the same as start char, so one char message
                DebugCommand.SendDebugMessage("Skipping section because of remchars (length would be " + (e - s + 1) + ")");
				continue;
			}
			originaltext = str.substring(s, e + 1);
			DebugCommand.SendDebugMessage("Section text: " + originaltext);
			Color color = null;
			boolean bold = false, italic = false, underlined = false, strikethrough = false, obfuscated = false;
            String openlink = null;
            section.Formatters.sort(Comparator.comparing(cf2 -> cf2.priority.GetValue())); //Apply the highest last, to overwrite previous ones
			for (ChatFormatter formatter : section.Formatters) {
				DebugCommand.SendDebugMessage("Applying formatter: " + formatter);
				if (formatter.onmatch != null)
					originaltext = formatter.onmatch.apply(originaltext, formatter);
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
				newtp.setClickEvent(TellrawEvent.create(TellrawEvent.ClickAction.OPEN_URL,
						(section.Matches.size() > 0 ? openlink.replace("$1", section.Matches.get(0)) : openlink)))
						.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT,
								new TellrawPart("Click to open").setColor(Color.Blue)));
			}
			tp.addExtra(newtp);
		}
		header("ChatFormatter.Combine done");
	}

	private static void sendMessageWithPointer(String str, int... pointer) {
		DebugCommand.SendDebugMessage(str);
		StringBuilder sb = new StringBuilder(str.length());
		Arrays.sort(pointer);
		for (int i = 0; i < pointer.length; i++) {
			for (int j = 0; j < pointer[i] - (i > 0 ? pointer[i - 1] + 1 : 0); j++)
				sb.append(' ');
			if (pointer[i] == (i > 0 ? pointer[i - 1] : -1))
				continue;
			sb.append('^');
		}
		DebugCommand.SendDebugMessage(sb.toString());
	}

	private static void header(String message) {
		DebugCommand.SendDebugMessage("\n--------\n" + message + "\n--------\n");
	}
}
