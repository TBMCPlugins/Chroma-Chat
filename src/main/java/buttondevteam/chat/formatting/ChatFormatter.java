package buttondevteam.chat.formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import buttondevteam.chat.ChatProcessing;
import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.lib.chat.*;

/**
 * A {@link ChatFormatter} shows what formatting to use based on regular expressions. {@link ChatFormatter#Combine(List, String, TellrawPart)} is used to turn it into a {@link TellrawPart}, combining
 * intersecting parts found, for example when {@code _abc*def*ghi_} is said in chat, it'll turn it into an underlined part, then an underlined <i>and italics</i> part, finally an underlined part
 * again.
 * 
 * @author NorbiPeti
 *
 */
public final class ChatFormatter {
	private ChatFormatterBuilder builder;

	public ChatFormatter(ChatFormatterBuilder builder) {
		this.builder = builder;
	}

	public static void Combine(List<ChatFormatter> formatters, String str, TellrawPart tp) {
		/*
		 * This method assumes that there is always a global formatter
		 */
		header("ChatFormatter.Combine begin");
		ArrayList<FormattedSection> sections = new ArrayList<FormattedSection>();
		for (ChatFormatter formatter : formatters) {
			Matcher matcher = formatter.builder.regex.matcher(str);
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
						formatter.builder.removecharcount, formatter.builder.removecharcount, formatter.builder.range);
				sections.add(section);
			}
		}
		sections.sort((s1, s2) -> s1.Start == s2.Start
				? s1.End == s2.End ? Integer.compare(s2.Formatters.get(0).builder.priority.GetValue(),
						s1.Formatters.get(0).builder.priority.GetValue()) : Integer.compare(s2.End, s1.End)
				: Integer.compare(s1.Start, s2.Start));

		header("Range section conversion");
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
				sendMessageWithPointer(str, section.Start, section.End);
				continue;
			}
			if (!escaped) {
				if (combined.stream().anyMatch(s -> s.IsRange && (s.Start == section.Start
						|| (s.Start < section.Start ? s.End >= section.Start : s.Start <= section.End)))) {
					DebugCommand.SendDebugMessage("Range " + section + " overlaps with a combined section, ignoring.");
					sendMessageWithPointer(str, section.Start, section.End);
					continue;
				}
				if (section.Start == takenStart || (section.Start > takenStart && section.Start < takenEnd)) {
					/*
					 * if (nextSection.containsKey(section.Formatters.get(0)) ? section.RemCharFromStart <= takenEnd - takenStart : section.RemCharFromStart > takenEnd - takenStart) {
					 */
					if (section.RemCharFromStart < takenEnd - takenStart) {
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
				takenEnd = section.Start + section.RemCharFromStart;
				takenFormatter = section.Formatters.get(0);
				if (nextSection.containsKey(section.Formatters.get(0))) {
					FormattedSection s = nextSection.remove(section.Formatters.get(0));
					s.End = section.Start + section.RemCharFromStart - 1;
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
				DebugCommand.SendDebugMessage("Skipping section: " + section);
				sendMessageWithPointer(str, section.Start, section.End);
				escaped = false; // Reset escaping if applied, like if we're at the '*' in '\*'
			}
		}

		header("Section combining");
		sections = combined;
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
				if (firstSection.RemCharFromStart < sections.get(i).RemCharFromStart)
					firstSection.RemCharFromStart = sections.get(i).RemCharFromStart;
				if (firstSection.RemCharFromEnd < sections.get(i).RemCharFromEnd)
					firstSection.RemCharFromEnd = sections.get(i).RemCharFromEnd;
				DebugCommand.SendDebugMessage("To section " + firstSection);
				sendMessageWithPointer(str, firstSection.Start, firstSection.End);
				sections.remove(i);
				found = true;
			} else if (firstSection.End > sections.get(i).Start && firstSection.Start < sections.get(i).End) {
				int[][][] rc = new int[3][2][2]; // Remove characters - Section start/end positions
				// [section number][start/end][remchar start/end]
				rc[0][0] = new int[] { firstSection.Start, firstSection.Start + firstSection.RemCharFromStart };
				rc[0][1] = new int[] { firstSection.End - firstSection.RemCharFromEnd, firstSection.End }; // Keep it in ascending order
				// The third section doesn't have characters to remove yet
				rc[2] = new int[][] {
						{ sections.get(i).Start, sections.get(i).Start + sections.get(i).RemCharFromStart },
						{ sections.get(i).End - sections.get(i).RemCharFromEnd, sections.get(i).End } }; // Keep it in ascending order
				int origend = firstSection.End;
				firstSection.End = sections.get(i).Start - 1;
				int origend2 = sections.get(i).End;
				boolean switchends;
				if (switchends = origend2 < origend) {
					int tmp = origend;
					origend = origend2;
					origend2 = tmp;
				}
				// int rc1start, rc1end, rc2start, rc2end, rc3start, rc3end; // Remove characters - TODO: Store positions
				FormattedSection section = new FormattedSection(firstSection.Formatters, sections.get(i).Start, origend,
						firstSection.Matches, (short) 0, (short) 0, false);
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
				System.out.println("RC start");
				for (short ii = 0; ii < 3; ii += 2) // Only check first and third section
					for (short iii = 0; iii < 2; iii++) {
						final int startorend = iii == 0 ? section.Start : section.End;
						if (rc[ii][iii][0] <= startorend && rc[ii][iii][1] >= startorend) {
							final String startorendText = iii == 0 ? "Start" : "End";
							System.out.println("rc[" + ii + "][" + iii + "][0] <= section." + startorendText + " && rc["
									+ ii + "][" + iii + "][1] >= section." + startorendText);
							System.out.println(rc[ii][iii][0] + " <= " + startorend + " && " + rc[ii][iii][1] + " >= "
									+ startorend);
							rc[1][iii] = new int[] { startorend, rc[ii][iii][1] };
							rc[ii][iii][1] = startorend - 1;
							System.out.println("rc[1][" + iii + "]: " + rc[1][iii][0] + " " + rc[1][iii][1]);
							System.out.println("rc[" + ii + "][" + iii + "][1]: " + rc[ii][iii][1]);
						}
					}
				System.out.println("RC done");
				Function<int[], Integer> getRemCharStart = arr -> arr[1] - arr[0] < 0 ? 0 : arr[1] - arr[0];
				firstSection.RemCharFromStart = (short) (int) getRemCharStart.apply(rc[0][0]);
				firstSection.RemCharFromEnd = (short) (int) getRemCharStart.apply(rc[0][1]);
				section.RemCharFromStart = (short) (int) getRemCharStart.apply(rc[1][0]);
				section.RemCharFromEnd = (short) (int) getRemCharStart.apply(rc[1][1]);
				thirdFormattedSection.RemCharFromStart = (short) (int) getRemCharStart.apply(rc[2][0]);
				thirdFormattedSection.RemCharFromEnd = (short) (int) getRemCharStart.apply(rc[2][1]);

				ArrayList<FormattedSection> sts = sections;
				Predicate<FormattedSection> removeIfNeeded = s -> {
					if (s.Start < 0 || s.End < 0 || s.Start > s.End || s.RemCharFromStart < 0 || s.RemCharFromEnd < 0) {
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
				if (j < sections.size() && sections.get(j).End - sections.get(j).RemCharFromEnd < sections.get(j).Start
						+ sections.get(j).RemCharFromStart) {
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
					sections.sort((s1,
							s2) -> s1.Start == s2.Start ? s1.End == s2.End
									? Integer.compare(s2.Formatters.get(0).builder.priority.GetValue(),
											s1.Formatters.get(0).builder.priority.GetValue())
									: Integer.compare(s2.End, s1.End) : Integer.compare(s1.Start, s2.Start));
				} else
					cont = false;
			}
		}

		header("Section applying");
		for (int i = 0; i < sections.size(); i++) {
			FormattedSection section = sections.get(i);
			DebugCommand.SendDebugMessage("Applying section: " + section);
			String originaltext;
			int start = section.Start + section.RemCharFromStart, end = section.End + 1 - section.RemCharFromEnd;
			DebugCommand.SendDebugMessage("Start: " + start + " - End: " + end);
			sendMessageWithPointer(str, start, end);
			originaltext = str.substring(start, end);
			DebugCommand.SendDebugMessage("Section text: " + originaltext);
			Color color = null;
			boolean bold = false, italic = false, underlined = false, strikethrough = false, obfuscated = false;
			String openlink = null;
			section.Formatters.sort((cf2, cf1) -> cf1.builder.priority.compareTo(cf2.builder.priority));
			for (ChatFormatter formatter : section.Formatters) {
				DebugCommand.SendDebugMessage("Applying formatter: " + formatter);
				if (formatter.builder.onmatch != null)
					originaltext = formatter.builder.onmatch.apply(originaltext, formatter.builder);
				if (formatter.builder.color != null)
					color = formatter.builder.color;
				if (formatter.builder.bold)
					bold = true;
				if (formatter.builder.italic)
					italic = true;
				if (formatter.builder.underlined)
					underlined = true;
				if (formatter.builder.strikethrough)
					strikethrough = true;
				if (formatter.builder.obfuscated)
					obfuscated = true;
				if (formatter.builder.openlink != null)
					openlink = formatter.builder.openlink;
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
		header("ChatFormatter.Combine done");
	}

	@Override
	public String toString() {
		return new StringBuilder("F(").append(builder.color).append(", ")
				.append((builder.bold ? "bold" : "") + (builder.italic ? "italic" : "")
						+ (builder.underlined ? "underlined" : "") + (builder.strikethrough ? "strikethrough" : "")
						+ (builder.obfuscated ? "obfuscated" : ""))
				.append(", ").append(builder.openlink).append(", ").append(builder.priority).append(", ")
				.append(builder.regex).append(")").toString(); // TODO: Lombok
	}

	/**
	 * 
	 * @param str
	 * @param pointer
	 *            This must be ordered ascending
	 */
	private static void sendMessageWithPointer(String str, int... pointer) {
		DebugCommand.SendDebugMessage(str);
		StringBuilder sb = new StringBuilder(str.length());
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
