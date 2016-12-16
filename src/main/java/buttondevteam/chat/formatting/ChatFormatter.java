package buttondevteam.chat.formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.lib.chat.*;

public final class ChatFormatter {
	private Pattern regex;
	private Format format;
	private Color color;
	private Function<String, String> onmatch;
	private String openlink;
	private Priority priority;
	private short removecharcount = 0;
	private short removecharpos = -1;
	private boolean isrange;

	public ChatFormatter(Pattern regex, Format format, Color color, Function<String, String> onmatch, String openlink,
			Priority priority, short removecharcount, short removecharpos, boolean isrange) {
		this.regex = regex;
		this.format = format;
		this.color = color;
		this.onmatch = onmatch;
		this.openlink = openlink;
		this.priority = Priority.High;
		this.removecharcount = removecharcount;
		this.removecharpos = removecharpos;
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
						formatter.removecharcount, formatter.removecharcount, formatter.removecharpos,
						formatter.isrange);
				sections.add(section);
			}
		}
		sections.sort((s1, s2) -> {
			if (s1.Start == s2.Start)
				return Integer.compare(s1.End, s2.End);
			else
				return Integer.compare(s1.Start, s2.Start);
		});
		List<FormattedSection> combined = new ArrayList<>();
		for (int i = 0; i < sections.size(); i++) {
			// Set ending to -1 until closed with another 1 long "section" - only do this if IsRange is true
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
					format = formatter.format.getFlag(); // TODO: Fix
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
}
