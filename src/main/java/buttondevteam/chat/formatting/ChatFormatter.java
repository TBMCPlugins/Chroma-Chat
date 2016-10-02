package buttondevteam.chat.formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import buttondevteam.chat.commands.ucmds.admin.DebugCommand;

public final class ChatFormatter {
	private Pattern regex;
	private Format format;
	private Color color;
	private Function<String, String> onmatch;
	private String openlink;
	private Priority priority;
	private short removecharcount = 0;
	private short removecharpos = -1;

	public ChatFormatter(Pattern regex, Format format, Color color, Function<String, String> onmatch, String openlink,
			Priority priority, short removecharcount, short removecharpos) {
		this.regex = regex;
		this.format = format;
		this.color = color;
		this.onmatch = onmatch;
		this.openlink = openlink;
		this.priority = Priority.High;
		this.removecharcount = removecharcount;
		this.removecharpos = removecharpos;
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
						formatter.removecharcount, formatter.removecharcount, formatter.removecharpos);
				sections.add(section);
			}
		}
		sections.sort((s1, s2) -> {
			if (s1.Start == s2.Start)
				return Integer.compare(s1.End, s2.End);
			else
				return Integer.compare(s1.Start, s2.Start);
		});
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
				firstSection.RemCharPos.addAll(sections.get(i).RemCharPos);
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
						firstSection.Matches, sections.get(i).RemCharFromStart, firstSection.RemCharFromEnd,
						Collections.emptyList());
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
				for (int x = 0; x < firstSection.RemCharPos.size(); x++) {
					if (firstSection.RemCharPos.get(x) > firstSection.End) {
						if (firstSection.RemCharPos.get(x) > section.End)
							thirdFormattedSection.RemCharPos.add(
									firstSection.RemCharPos.get(x) - thirdFormattedSection.Start + firstSection.Start);
						else
							section.RemCharPos.add(firstSection.RemCharPos.get(x) - section.Start + firstSection.Start);
						firstSection.RemCharPos.remove(x--);
					}
				}
				DebugCommand.SendDebugMessage("To sections 1:" + firstSection + "");
				DebugCommand.SendDebugMessage("  2:" + section + "");
				DebugCommand.SendDebugMessage("  3:" + thirdFormattedSection);
				found = true;
			}
			for (int j = i - 1; j <= i + 1; j++) {
				if (j < sections.size() && sections.get(j).End < sections.get(j).Start) {
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
					sections.sort((s1, s2) -> {
						if (s1.Start == s2.Start)
							return Integer.compare(s1.End, s2.End);
						else
							return Integer.compare(s1.Start, s2.Start);
					});
				} else
					cont = false;
			}
		}
		for (int i = 0; i < sections.size(); i++) {
			FormattedSection section = sections.get(i);
			DebugCommand.SendDebugMessage("Applying section: " + section);
			String originaltext;
			int start = section.Start + section.RemCharFromStart, end = section.End + 1 - section.RemCharFromEnd; // TODO: RemCharPos
			StringBuilder textsb = new StringBuilder(str.substring(start, end));
			for (int x = 0; x < section.RemCharPos.size(); x++)
				if (section.RemCharPos.get(x) != -1)
					textsb.deleteCharAt(section.RemCharPos.get(x));
			originaltext = textsb.toString();
			DebugCommand.SendDebugMessage("Section text: " + originaltext);
			Color color = null;
			int format = 0;
			String openlink = null;
			section.Formatters.sort((cf2, cf1) -> cf1.priority.compareTo(cf2.priority));
			for (ChatFormatter formatter : section.Formatters) {
				DebugCommand.SendDebugMessage("Applying formatter: " + formatter);
				if (formatter.onmatch != null)
					originaltext = formatter.onmatch.apply(originaltext);
				if (formatter.color != null)
					color = formatter.color;
				if (formatter.format != null)
					format = formatter.format.flag; //TODO: Fix
				if (formatter.openlink != null)
					openlink = formatter.openlink;
			}
			TellrawPart newtp = new TellrawPart("");
			newtp.setText(originaltext);
			if (color != null)
				newtp.setColor(color);
			if (format != 0)
				newtp.setFormat(format);
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
		return new StringBuilder("F(").append(color).append(", ").append(format).append(", ").append(openlink)
				.append(", ").append(priority).append(")").toString();
	}

	public enum Format implements TellrawSerializableEnum { // TODO: Flag!
		Bold("bold"), Underlined("underlined"), Italic("italic"), Strikethrough("strikethrough"), Obfuscated(
				"obfuscated");
		// TODO: Add format codes to /u c <mode>
		private String name;

		Format(String name) {
			this.name = name;
			this.flag = 1 << this.ordinal();
		}

		@Override
		public String getName() {
			return name;
		}

		private final int flag;

		public int getFlag() {
			return flag;
		}
	}

	public enum Color implements TellrawSerializableEnum {
		Black("black"), DarkBlue("dark_blue"), DarkGreen("dark_green"), DarkAqua("dark_aqua"), DarkRed(
				"dark_red"), DarkPurple("dark_purple"), Gold("gold"), Gray("gray"), DarkGray("dark_gray"), Blue(
						"blue"), Green("green"), Aqua("aqua"), Red(
								"red"), LightPurple("light_purple"), Yellow("yellow"), White("white"), RPC("rpc");

		private String name;

		Color(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}

	public enum Priority {
		Low(0), Normal(1), High(2);
		private int val;

		Priority(int v) {
			val = v;
		}

		public int GetValue() {
			return val;
		}
	}
}
