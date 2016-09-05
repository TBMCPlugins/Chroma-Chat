package buttondevteam.thebuttonmcchat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import buttondevteam.thebuttonmcchat.commands.ucmds.admin.DebugCommand;

public final class ChatFormatter {
	private Pattern regex;
	private Format format;
	private Color color;
	private Function<String, String> onmatch;
	private String openlink;
	private Priority priority;
	private String replacewith;

	private static final String[] RainbowPresserColors = new String[] { "red", "gold", "yellow", "green", "blue",
			"dark_purple" }; // TODO: Move out to ChatProcessing

	public ChatFormatter(Pattern regex, Format format, Color color, Function<String, String> onmatch, String openlink,
			Priority priority, String replacewith) {
		this.regex = regex;
		this.format = format;
		this.color = color;
		this.onmatch = onmatch;
		this.openlink = openlink;
		this.priority = Priority.High;
		this.replacewith = replacewith;
	}

	public static String Combine(List<ChatFormatter> formatters, String str) {
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
				FormattedSection section = new FormattedSection(formatter, matcher.start(), matcher.end() - 1, groups);
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
			if (sections.get(i - 1).End > sections.get(i).Start && sections.get(i - 1).Start < sections.get(i).End) {
				DebugCommand.SendDebugMessage("Combining sections " + sections.get(i - 1) + " and " + sections.get(i));
				int origend = sections.get(i - 1).End;
				sections.get(i - 1).End = sections.get(i).Start - 1;
				int origend2 = sections.get(i).End;
				boolean switchends;
				if (switchends = origend2 < origend) {
					int tmp = origend;
					origend = origend2;
					origend2 = tmp;
				}
				FormattedSection section = new FormattedSection(sections.get(i - 1).Formatters, sections.get(i).Start,
						origend, sections.get(i - 1).Matches);
				section.Formatters.addAll(sections.get(i).Formatters);
				section.Matches.addAll(sections.get(i).Matches);
				sections.add(i, section);
				nextindex++;
				FormattedSection thirdFormattedSection = sections.get(i + 1);
				if (switchends) { // Use the properties of the first section not the second one
					thirdFormattedSection.Formatters.clear();
					thirdFormattedSection.Formatters.addAll(sections.get(i - 1).Formatters);
					thirdFormattedSection.Matches.clear();
					thirdFormattedSection.Matches.addAll(sections.get(i - 1).Matches);
				}
				thirdFormattedSection.Start = origend + 1;
				thirdFormattedSection.End = origend2;
				DebugCommand.SendDebugMessage("To sections 1:" + sections.get(i - 1) + "");
				DebugCommand.SendDebugMessage("  2:" + section + "");
				DebugCommand.SendDebugMessage("  3:" + thirdFormattedSection);
				found = true;
			}
			if (sections.get(i - 1).Start == sections.get(i).Start && sections.get(i - 1).End == sections.get(i).End) {
				DebugCommand.SendDebugMessage("Combining sections " + sections.get(i - 1) + " and " + sections.get(i));
				sections.get(i - 1).Formatters.addAll(sections.get(i).Formatters);
				sections.get(i - 1).Matches.addAll(sections.get(i).Matches);
				DebugCommand.SendDebugMessage("To section " + sections.get(i - 1));
				sections.remove(i);
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
		StringBuilder finalstring = new StringBuilder();
		for (int i = 0; i < sections.size(); i++) {
			FormattedSection section = sections.get(i);
			DebugCommand.SendDebugMessage("Applying section: " + section);
			String originaltext = str.substring(section.Start, section.End + 1);
			DebugCommand.SendDebugMessage("Originaltext: " + originaltext);
			Color color = null;
			Format format = null;
			String openlink = null;
			String replacewith = null;
			section.Formatters.sort((cf1, cf2) -> cf1.priority.compareTo(cf2.priority));
			for (ChatFormatter formatter : section.Formatters) {
				DebugCommand.SendDebugMessage("Applying formatter: " + formatter);
				if (formatter.onmatch != null)
					originaltext = formatter.onmatch.apply(originaltext);
				if (formatter.color != null)
					color = formatter.color;
				if (formatter.format != null)
					format = formatter.format;
				if (formatter.openlink != null)
					openlink = formatter.openlink;
				if (formatter.replacewith != null)
					replacewith = formatter.replacewith;
			}
			finalstring.append(",{\"text\":\"");
			if (replacewith != null)
				finalstring.append(replacewith.replace("$1", section.Matches.get(0)));
			else
				finalstring.append(originaltext);
			finalstring.append("\"");
			if (color != null) {
				finalstring.append(",\"color\":\"");
				finalstring.append(color.name);
				finalstring.append("\"");
			}
			if (format != null) {
				finalstring.append(",\"");
				finalstring.append(format.name);
				finalstring.append("\":\"true\"");
			}
			if (openlink != null && openlink.length() > 0) {
				finalstring.append(String.format(
						",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to open\",\"color\":\"blue\"}]}}",
						(section.Matches.size() > 0 ? openlink.replace("$1", section.Matches.get(0)) : openlink)));
			}
			finalstring.append("}");
		}
		DebugCommand.SendDebugMessage("Finalstring: " + finalstring);
		return finalstring.toString();

	}

	@Override
	public String toString() {
		return new StringBuilder("F(").append(color).append(", ").append(format).append(", ").append(openlink)
				.append(", ").append(priority).append(")").toString();
	}

	public enum Format { // TODO: Flag?
		Bold("bold"), Underlined("underlined"), Italic("italic"), Strikethrough("strikethrough"), Obfuscated(
				"obfuscated");
		// TODO: Add format codes to /u c <mode>
		private String name;

		Format(String name) {
			this.name = name;
		}

		public String GetName() {
			return name;
		}
	}

	public enum Color {
		Black("black"), DarkBlue("dark_blue"), DarkGreen("dark_green"), DarkAqua("dark_aqua"), DarkRed(
				"dark_red"), DarkPurple("dark_purple"), Gold("gold"), Gray("gray"), DarkGray("dark_gray"), Blue(
						"blue"), Green("green"), Aqua("aqua"), Red(
								"red"), LightPurple("light_purple"), Yellow("yellow"), White("white"), RPC("rpc");

		private String name;

		Color(String name) {
			this.name = name;
		}

		public String GetName() {
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
