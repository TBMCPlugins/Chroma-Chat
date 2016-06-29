package io.github.norbipeti.thebuttonmcchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatFormatter {
	private Pattern regex;
	private Format format;
	private Color color;
	private Predicate<String> onmatch;
	private String openlink;
	private Priority priority;

	private static final String[] RainbowPresserColors = new String[] { "red",
			"gold", "yellow", "green", "blue", "dark_purple" };

	public ChatFormatter(Pattern regex, Format format) {
		this.regex = regex;
		this.format = format;
		this.priority = Priority.High;
	}

	public ChatFormatter(Pattern regex, Format format, String openlink) { // TODO:
																			// Openlink
																			// won't
																			// be
																			// replaced,
																			// as
																			// the
																			// original
																			// string
																			// should
																			// be;
																			// replace
																			// $1
																			// with
																			// match
		// TODO: Get indexes and work with those
		this.regex = regex;
		this.format = format;
		this.openlink = openlink;
		this.priority = Priority.High;
	}

	public ChatFormatter(Pattern regex, Color color, String openlink,
			Priority priority) {
		this.regex = regex;
		this.color = color;
		this.openlink = openlink;
		this.priority = priority;
	}

	public ChatFormatter(Pattern regex, Color color, Priority priority) {
		this.regex = regex;
		this.color = color;
		this.priority = priority;
	}

	public ChatFormatter(Pattern regex, Color color, Predicate<String> onmatch,
			Priority priority) {
		this.regex = regex;
		this.color = color;
		this.onmatch = onmatch;
		this.priority = priority;
	}

	public static String Combine(List<ChatFormatter> formatters, String str) {
		/*
		 * This method assumes that there are always a global formatter
		 */
		ArrayList<FormattedSection> sections = new ArrayList<ChatFormatter.FormattedSection>();
		for (ChatFormatter formatter : formatters) {
			Matcher matcher = formatter.regex.matcher(str);
			while (matcher.find()) {
				System.out.println("Found match from " + matcher.start()
						+ " to " + (matcher.end() - 1));
				ArrayList<String> groups = new ArrayList<String>();
				for (int i = 0; i < matcher.groupCount(); i++)
					groups.add(matcher.group(i + 1));
				if (groups.size() > 0)
					System.out.println("First group: " + groups.get(0));
				FormattedSection section = formatter.new FormattedSection(
						formatter, matcher.start(), matcher.end() - 1, groups);
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
			System.out.println("i: " + i);
			if (sections.get(i - 1).End >= sections.get(i).Start) {
				System.out.println("Combining sections ("
						+ sections.get(i - 1).Start + " - "
						+ sections.get(i - 1).End + ") and ("
						+ sections.get(i).Start + " - " + sections.get(i).End
						+ ")");
				int origend = sections.get(i - 1).End;
				sections.get(i - 1).End = sections.get(i).Start - 1;

				FormattedSection section = sections.get(i - 1).Formatters
						.get(0).new FormattedSection(
						sections.get(i - 1).Formatters, sections.get(i).Start,
						origend, sections.get(i - 1).Matches);
				section.Formatters.addAll(sections.get(i).Formatters);
				section.Matches.addAll(sections.get(i).Matches);
				sections.add(i, section);
				nextindex++;
				sections.get(i + 1).Start = origend + 1;
				System.out.println("To sections 1:("
						+ sections.get(i - 1).Start + " - "
						+ sections.get(i - 1).End + ")");
				System.out.println("  2:(" + section.Start + " - "
						+ section.End + ")");
				System.out.println("  3:(" + sections.get(i + 1).Start + " - "
						+ sections.get(i + 1).End + ")");
				found = true;
			}
			if (sections.get(i - 1).Start == sections.get(i).Start
					&& sections.get(i - 1).End == sections.get(i).End) {
				sections.get(i - 1).Formatters
						.addAll(sections.get(i).Formatters);
				sections.get(i - 1).Matches.addAll(sections.get(i).Matches);
			}
			i = nextindex - 1;
			i++;
			if (i >= sections.size()) {
				if (found) {
					i = 1;
					found = false;
				} else
					cont = false;
			}
		}
		StringBuilder finalstring = new StringBuilder();
		for (int i = 0; i < sections.size(); i++) {
			if (sections.get(i).End < sections.get(i).Start) {
				System.out.println("Removing section: " + sections.get(i).Start
						+ " - " + sections.get(i).End);
				sections.remove(i);
				i--;
				continue;
			}
			FormattedSection section = sections.get(i);
			System.out.println("Applying section: " + section.Start + " - "
					+ section.End);
			String originaltext = str.substring(section.Start, section.End + 1);
			System.out.println("Originaltext: " + originaltext);
			finalstring.append(",{\"text\":\""); //TODO: Bool replace
			finalstring.append(originaltext);
			finalstring.append("\"");
			Color color = null;
			Format format = null;
			String openlink = null;
			Priority priority = null;
			for (ChatFormatter formatter : section.Formatters) {
				System.out.println("Applying formatter: Color: "
						+ formatter.color + " Format: " + formatter.format
						+ " Openlink: " + formatter.openlink);
				if (formatter.onmatch == null
						|| formatter.onmatch.test(originaltext)) {
					if (priority == null
							|| priority.GetValue() < formatter.priority
									.GetValue()) {
						color = formatter.color;
						format = formatter.format; // TODO: Don't overwrite
													// parts, and work until all
													// of them are combined
						openlink = formatter.openlink;
						priority = formatter.priority;
					}
				} else
					System.out.println("Onmatch predicate returned false.");
			}
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
				finalstring
						.append(String
								.format(",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"%s\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to open\",\"color\":\"blue\"}]}}",
										(section.Matches.size() > 0 ? openlink
												.replace("$1",
														section.Matches.get(0))
												: openlink)));
			}
			finalstring.append("}");
		}
		return finalstring.toString(); // TODO
	}

	public enum Format { // TODO: Flag?
		Bold("bold"), Underlined("underlined"), Italic("italic"), Strikethrough(
				"strikethrough"), Obfuscated("obfuscated");
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
		Black("black"), DarkBlue("dark_blue"), DarkGreen("dark_green"), DarkAqua(
				"dark_aqua"), DarkRed("dark_red"), DarkPurple("dark_purple"), Gold(
				"gold"), Gray("gray"), DarkGray("dark_gray"), Blue("blue"), Green(
				"green"), Aqua("aqua"), Red("red"), LightPurple("light_purple"), Yellow(
				"yellow"), White("white"), RPC("rpc");

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

	private class FormattedSection {
		public int Start;
		public int End;
		public ArrayList<ChatFormatter> Formatters = new ArrayList<ChatFormatter>();
		public ArrayList<String> Matches = new ArrayList<String>();

		public FormattedSection(ChatFormatter formatter, int start, int end,
				ArrayList<String> matches) {
			Start = start;
			End = end;
			Formatters.add(formatter);
			Matches.addAll(matches);
		}

		public FormattedSection(Collection<ChatFormatter> formatters,
				int start, int end, ArrayList<String> matches) {
			Start = start;
			End = end;
			Formatters.addAll(formatters);
			Matches.addAll(matches);
		}
	}
}
