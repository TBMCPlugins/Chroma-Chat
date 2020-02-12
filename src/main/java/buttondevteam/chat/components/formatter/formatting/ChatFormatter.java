package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.lib.architecture.IHaveConfig;
import buttondevteam.lib.chat.Color;
import lombok.val;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A {@link ChatFormatter} shows what formatting to use based on regular expressions. {@link ChatFormatter#Combine(List, String, TellrawPart, IHaveConfig)} is used to turn it into a {@link TellrawPart}, combining
 * intersecting parts found, for example when {@code _abc*def*ghi_} is said in chat, it'll turn it into an underlined part, then an underlined <i>and italics</i> part, finally an underlined part
 * again.
 *
 * @author NorbiPeti
 */ //TODO: Update doc
public final class ChatFormatter {
	private ChatFormatter() {
	}

	@FunctionalInterface
	public interface TriFunc<T1, T2, T3, R> {
		R apply(T1 x1, T2 x2, T3 x3);
	}

	public static void Combine(List<MatchProviderBase> formatters, String str, TellrawPart tp, IHaveConfig config, FormatSettings defaults) {
		/*
		 * A global formatter is no longer needed
		 */
		header("ChatFormatter.Combine begin");
		ArrayList<FormattedSection> sections = new ArrayList<>();

		if (config != null) //null if testing
			formatters.removeIf(cf -> !cf.enabled(config).get()); //Remove disabled formatters
		var excluded = new ArrayList<int[]>();
		/*
		 * 0: Start - 1: End index
		 */
		val remchars = new ArrayList<int[]>();

		createSections(formatters, str, sections, excluded, remchars, defaults);
		sortSections(sections);

		header("Section combining");
		combineSections(str, sections);

		header("Section applying");
		applySections(str, tp, sections, remchars);
		header("ChatFormatter.Combine done");
	}

	private static void createSections(List<MatchProviderBase> formatters, String str, ArrayList<FormattedSection> sections,
									   ArrayList<int[]> excludedAreas, ArrayList<int[]> removedCharacters, FormatSettings defaults) {
		sections.add(new FormattedSection(defaults, 0, str.length() - 1, Collections.emptyList())); //Add entire message
		while (formatters.size() > 0) {
			for (var iterator = formatters.iterator(); iterator.hasNext(); ) {
				MatchProviderBase formatter = iterator.next();
				DebugCommand.SendDebugMessage("Checking provider: " + formatter);
				var sect = formatter.getNextSection(str, excludedAreas, removedCharacters);
				if (sect != null)
					sections.add(sect);
				if (formatter.isFinished())
					iterator.remove();
			}
		}
	}

	private static void combineSections(String str, ArrayList<FormattedSection> sections) {
		for (int i = 1; i < sections.size(); i++) {
			DebugCommand.SendDebugMessage("i: " + i);
			final FormattedSection firstSection;
			final FormattedSection lastSection;
			{
				FormattedSection firstSect = sections.get(i - 1);
				FormattedSection lastSect = sections.get(i);
				if (firstSect.Start > lastSect.Start) { //The first can't start later
					var section = firstSect;
					firstSect = lastSect;
					lastSect = section;
				}
				firstSection = firstSect;
				lastSection = lastSect;
			}
			DebugCommand.SendDebugMessage("Combining sections " + firstSection);
			ChatFormatUtils.sendMessageWithPointer(str, firstSection.Start, firstSection.End);
			DebugCommand.SendDebugMessage(" and " + lastSection);
			ChatFormatUtils.sendMessageWithPointer(str, lastSection.Start, lastSection.End);
			if (firstSection.Start == lastSection.Start && firstSection.End == lastSection.End) {
				firstSection.Settings.copyFrom(lastSection.Settings);
				firstSection.Matches.addAll(lastSection.Matches);
				DebugCommand.SendDebugMessage("To section " + firstSection);
				ChatFormatUtils.sendMessageWithPointer(str, firstSection.Start, firstSection.End);
				sections.remove(i);
				i = 0;
				sortSections(sections);
				continue;
			} else if (firstSection.End > lastSection.Start && firstSection.Start < lastSection.End) {
				int origend2 = firstSection.End;
				firstSection.End = lastSection.Start - 1;
				int origend = lastSection.End;
				FormattedSection section = new FormattedSection(firstSection.Settings, lastSection.Start, origend,
					firstSection.Matches);
				section.Settings.copyFrom(lastSection.Settings);
				section.Matches.addAll(lastSection.Matches); // TODO: Clean
				sections.add(i, section);
				// Use the properties of the first section not the second one
				lastSection.Settings = firstSection.Settings;
				lastSection.Matches.clear();
				lastSection.Matches.addAll(firstSection.Matches);

				lastSection.Start = origend + 1;
				lastSection.End = origend2;

				Predicate<FormattedSection> removeIfNeeded = s -> {
					if (s.Start < 0 || s.End < 0 || s.Start > s.End) {
						DebugCommand.SendDebugMessage("  Removed: " + s);
						ChatFormatUtils.sendMessageWithPointer(str, s.Start, s.End);
						sections.remove(s);
						return true;
					}
					return false;
				};

				DebugCommand.SendDebugMessage("To sections");
				if (!removeIfNeeded.test(firstSection)) {
					DebugCommand.SendDebugMessage("  1:" + firstSection + "");
					ChatFormatUtils.sendMessageWithPointer(str, firstSection.Start, firstSection.End);
				}
				if (!removeIfNeeded.test(section)) {
					DebugCommand.SendDebugMessage("  2:" + section + "");
					ChatFormatUtils.sendMessageWithPointer(str, section.Start, section.End);
				}
				if (!removeIfNeeded.test(lastSection)) {
					DebugCommand.SendDebugMessage("  3:" + lastSection);
					ChatFormatUtils.sendMessageWithPointer(str, lastSection.Start, lastSection.End);
				}
				i = 0;
			}
			sortSections(sections);
			if (i == 0) continue;
			for (int j = i - 1; j <= i + 1; j++) {
				if (j < sections.size() && sections.get(j).End < sections.get(j).Start) {
					DebugCommand.SendDebugMessage("Removing section: " + sections.get(j));
					ChatFormatUtils.sendMessageWithPointer(str, sections.get(j).Start, sections.get(j).End);
					sections.remove(j);
					j--;
					i = 0;
				}
			}
		}
	}

	private static void applySections(String str, TellrawPart tp, ArrayList<FormattedSection> sections, ArrayList<int[]> remchars) {
		TellrawPart lasttp = null;
		String lastlink = null;
		for (FormattedSection section : sections) {
			DebugCommand.SendDebugMessage("Applying section: " + section);
			String originaltext;
			int start = section.Start, end = section.End;
			DebugCommand.SendDebugMessage("Start: " + start + " - End: " + end);
			ChatFormatUtils.sendMessageWithPointer(str, start, end);
			/*DebugCommand.SendDebugMessage("RCS: "+remchars.stream().filter(rc -> rc[0] <= start && start <= rc[1]).count());
			DebugCommand.SendDebugMessage("RCE: "+remchars.stream().filter(rc -> rc[0] <= end && end <= rc[1]).count());
			DebugCommand.SendDebugMessage("RCI: "+remchars.stream().filter(rc -> start < rc[0] || rc[1] < end).count());*/
			val rci = remchars.stream().filter(rc -> (rc[0] <= start && rc[1] >= start)
				|| (rc[0] >= start && rc[1] <= end)
				|| (rc[0] <= end && rc[1] >= end)).sorted(Comparator.comparingInt(rc -> rc[0] * 10000 + rc[1])).toArray(int[][]::new);
			/*if (rcs.isPresent())
				s = rcs.get()[1] + 1;
			if (rce.isPresent())
				e = rce.get()[0] - 1;
			DebugCommand.SendDebugMessage("After RC - Start: " + s + " - End: " + e);
			if (e - s < 0) { //e-s==0 means the end char is the same as start char, so one char message
				DebugCommand.SendDebugMessage("Skipping section because of remchars (length would be " + (e - s + 1) + ")");
				continue;
			}*/
			DebugCommand.SendDebugMessage("Applying RC: " + Arrays.stream(rci).map(Arrays::toString).collect(Collectors.joining(", ", "[", "]")));
			originaltext = str.substring(start, end + 1);
			val sb = new StringBuilder(originaltext);
			for (int x = rci.length - 1; x >= 0; x--)
				sb.delete(Math.max(rci[x][0] - start, 0), Math.min(rci[x][1] - start, end) + 1); //Delete going backwards
			originaltext = sb.toString();
			if (originaltext.length() == 0) {
				DebugCommand.SendDebugMessage("Skipping section because of remchars");
				continue;
			}
			DebugCommand.SendDebugMessage("Section text: " + originaltext);
			String openlink = null;
			//section.Formatters.sort(Comparator.comparing(cf2 -> cf2.priority.GetValue())); //Apply the highest last, to overwrite previous ones
			TellrawPart newtp = new TellrawPart("");
			var settings = section.Settings;
			DebugCommand.SendDebugMessage("Applying settings: " + settings);
			if (settings.onmatch != null)
				originaltext = settings.onmatch.apply(originaltext, settings, section);
			if (settings.color != null)
				newtp.setColor(settings.color);
			if (settings.bold)
				newtp.setBold(true);
			if (settings.italic)
				newtp.setItalic(true);
			if (settings.underlined)
				newtp.setUnderlined(true);
			if (settings.strikethrough)
				newtp.setStrikethrough(true);
			if (settings.obfuscated)
				newtp.setObfuscated(true);
			if (settings.openlink != null)
				openlink = settings.openlink;
			if (settings.hoverText != null)
				newtp.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT, settings.hoverText));
			if (lasttp != null && newtp.getColor() == lasttp.getColor()
				&& newtp.isBold() == lasttp.isBold()
				&& newtp.isItalic() == lasttp.isItalic()
				&& newtp.isUnderlined() == lasttp.isUnderlined()
				&& newtp.isStrikethrough() == lasttp.isStrikethrough()
				&& newtp.isObfuscated() == lasttp.isObfuscated()
				&& Objects.equals(openlink, lastlink)) {
				DebugCommand.SendDebugMessage("This part has the same properties as the previous one, combining.");
				lasttp.setText(lasttp.getText() + originaltext);
				continue; //Combine parts with the same properties
			}
			lastlink = openlink;
			newtp.setText(originaltext);
			if (openlink != null && openlink.length() > 0) {
				newtp.setClickEvent(TellrawEvent.create(TellrawEvent.ClickAction.OPEN_URL,
					(section.Matches.size() > 0 ? openlink.replace("$1", section.Matches.get(0)) : openlink)))
					.setHoverEvent(TellrawEvent.create(TellrawEvent.HoverAction.SHOW_TEXT,
						new TellrawPart("Click to open").setColor(Color.Blue)));
			}
			tp.addExtra(newtp);
			lasttp = newtp;
		}
	}

	private static void sortSections(ArrayList<FormattedSection> sections) {
		sections.sort(
			(s1, s2) -> s1.Start == s2.Start
				? s1.End == s2.End ? 0 : Integer.compare(s1.End, s2.End) //TODO: Test
				: Integer.compare(s1.Start, s2.Start));
	}

	private static void header(String message) {
		DebugCommand.SendDebugMessage("\n--------\n" + message + "\n--------\n");
	}
}
