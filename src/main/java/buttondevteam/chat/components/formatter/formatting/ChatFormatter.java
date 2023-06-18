package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import buttondevteam.lib.architecture.IHaveConfig;
import lombok.val;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.Action.OPEN_URL;
import static net.kyori.adventure.text.event.ClickEvent.clickEvent;
import static net.kyori.adventure.text.event.HoverEvent.Action.SHOW_TEXT;
import static net.kyori.adventure.text.event.HoverEvent.hoverEvent;

/**
 * A {@link MatchProvider} finds where the given {@link FormatSettings} need to be applied. {@link ChatFormatter#Combine(List, String, TextComponent.Builder, IHaveConfig, FormatSettings)}} is used to turn it into a {@link TellrawPart}, combining
 * intersecting parts found, for example when {@code _abc*def*ghi_} is said in chat, it'll turn it into an underlined part, then an underlined <i>and italics</i> part, finally an underlined part
 * again.
 *
 * @author NorbiPeti
 */
public final class ChatFormatter {
	private ChatFormatter() {
	}

	@FunctionalInterface
	public interface TriFunc<T1, T2, T3, R> {
		R apply(T1 x1, T2 x2, T3 x3);
	}

	//synchronized: Some of the formatters are reused, see createSections(...)
	public static synchronized void Combine(List<MatchProviderBase> formatters, String str, TextComponent.Builder tp, IHaveConfig config, FormatSettings defaults) {
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

		escapeThings(str, excluded, remchars);

		sections.add(new FormattedSection(defaults, 0, str.length() - 1, Collections.emptyList())); //Add entire message
		var providers = formatters.stream().filter(mp -> mp instanceof RegexMatchProvider).collect(Collectors.toList());
		createSections(providers, str, sections, excluded, remchars);
		providers = formatters.stream().filter(mp -> mp instanceof StringMatchProvider).collect(Collectors.toList());
		createSections(providers, str, sections, excluded, remchars);
		providers = formatters.stream().filter(mp -> mp instanceof RangeMatchProvider).collect(Collectors.toList());
		createSections(providers, str, sections, excluded, remchars);
		sortSections(sections);

		header("Section combining");
		combineSections(str, sections);

		header("Section applying");
		applySections(str, tp, sections, remchars);
		header("ChatFormatter.Combine done");
	}

	private static void escapeThings(String str, ArrayList<int[]> ignoredAreas, ArrayList<int[]> remchars) {
		boolean escaped = false;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\\') {
				remchars.add(new int[]{i, i});
				ignoredAreas.add(new int[]{i + 1, i + 1});
				i++; //Ignore a potential second slash
			}
		}
	}

	private static void createSections(List<MatchProviderBase> formatters, String str, ArrayList<FormattedSection> sections,
	                                   ArrayList<int[]> excludedAreas, ArrayList<int[]> removedCharacters) {
		formatters.forEach(MatchProviderBase::reset); //Reset state information, as we aren't doing deep cloning
		while (formatters.size() > 0) {
			for (var iterator = formatters.iterator(); iterator.hasNext(); ) {
				MatchProviderBase formatter = iterator.next();
				DebugCommand.SendDebugMessage("Checking provider: " + formatter);
				var sect = formatter.getNextSection(str, excludedAreas, removedCharacters);
				if (sect != null) //Not excluding the area here because the range matcher shouldn't take it all
					sections.add(sect);
				if (formatter.isFinished()) {
					DebugCommand.SendDebugMessage("Provider finished");
					iterator.remove();
				}
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
				if (firstSect.Start > lastSect.Start //The first can't start later
					|| (firstSect.Start == lastSect.Start && firstSect.End < lastSect.End)) {
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
			} else if (firstSection.End >= lastSection.Start && firstSection.Start <= lastSection.End) {
				int middleStart = lastSection.Start;
				// |----|--    |------|
				// --|----|    -|----|-
				int middleEnd = Math.min(lastSection.End, firstSection.End);
				int lastSectEnd = Math.max(lastSection.End, firstSection.End);
				FormattedSection section = new FormattedSection(firstSection.Settings, middleStart, middleEnd,
					firstSection.Matches);
				section.Settings.copyFrom(lastSection.Settings);
				section.Matches.addAll(lastSection.Matches);
				sections.add(i, section);

				if (firstSection.End > lastSection.End) { //Copy first section info to last as the lastSection initially cuts the firstSection in half
					lastSection.Settings = FormatSettings.builder().build();
					lastSection.Settings.copyFrom(firstSection.Settings);
				}

				firstSection.End = middleStart - 1;
				lastSection.Start = middleEnd + 1;
				lastSection.End = lastSectEnd;

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
					DebugCommand.SendDebugMessage("  1:" + firstSection);
					ChatFormatUtils.sendMessageWithPointer(str, firstSection.Start, firstSection.End);
				}
				if (!removeIfNeeded.test(section)) {
					DebugCommand.SendDebugMessage("  2:" + section);
					ChatFormatUtils.sendMessageWithPointer(str, section.Start, section.End);
				}
				if (!removeIfNeeded.test(lastSection)) {
					DebugCommand.SendDebugMessage("  3:" + lastSection);
					ChatFormatUtils.sendMessageWithPointer(str, lastSection.Start, lastSection.End);
				}
				i = 0;
			}
			sortSections(sections);
		}
	}

	private static void applySections(String str, TextComponent.Builder tp, ArrayList<FormattedSection> sections, ArrayList<int[]> remchars) {
		TextComponent lasttp = null;
		String lastlink = null;
		for (FormattedSection section : sections) {
			DebugCommand.SendDebugMessage("Applying section: " + section);
			String originaltext;
			int start = section.Start, end = section.End;
			DebugCommand.SendDebugMessage("Start: " + start + " - End: " + end);
			ChatFormatUtils.sendMessageWithPointer(str, start, end);
			val rci = remchars.stream().filter(rc -> (rc[0] <= start && rc[1] >= start)
				|| (rc[0] >= start && rc[1] <= end)
				|| (rc[0] <= end && rc[1] >= end)).sorted(Comparator.comparingInt(rc -> rc[0] * 10000 + rc[1])).toArray(int[][]::new);
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
			var settings = section.Settings;
			DebugCommand.SendDebugMessage("Applying settings: " + settings);
			if (lasttp != null && hasSameDecorations(lasttp, settings) && Objects.equals(lastlink, settings.openlink)) {
				DebugCommand.SendDebugMessage("This part has the same properties as the previous one, combining.");
				lasttp = lasttp.content(lasttp.content() + originaltext);
				continue; //Combine parts with the same properties
			}
			TextComponent.@NotNull Builder newtp = text();
			if (settings.onmatch != null)
				originaltext = settings.onmatch.apply(originaltext, settings, section);
			if (settings.color != null)
				newtp.color(settings.color);
			if (settings.bold)
				newtp.decorate(TextDecoration.BOLD);
			if (settings.italic)
				newtp.decorate(TextDecoration.ITALIC);
			if (settings.underlined)
				newtp.decorate(TextDecoration.UNDERLINED);
			if (settings.strikethrough)
				newtp.decorate(TextDecoration.STRIKETHROUGH);
			if (settings.obfuscated)
				newtp.decorate(TextDecoration.OBFUSCATED);
			if (settings.openlink != null)
				openlink = settings.openlink;
			if (settings.hoverText != null)
				newtp.hoverEvent(hoverEvent(SHOW_TEXT, text(settings.hoverText)));
			if (lasttp != null) tp.append(lasttp);
			lastlink = openlink;
			newtp.content(originaltext);
			if (openlink != null && openlink.length() > 0) {
				if (section.Matches.size() > 0)
					openlink = openlink.replace("$1", section.Matches.get(0));
				newtp.clickEvent(clickEvent(OPEN_URL, openlink)).hoverEvent(hoverEvent(SHOW_TEXT, text("Click to open").color(NamedTextColor.BLUE)));
			}
			lasttp = newtp.build();
		}
		if (lasttp != null) tp.append(lasttp);
	}

	private static boolean hasSameDecorations(TextComponent c1, FormatSettings settings) {
		return Objects.equals(c1.color(), settings.color)
			&& c1.hasDecoration(TextDecoration.BOLD) == settings.bold
			&& c1.hasDecoration(TextDecoration.ITALIC) == settings.italic
			&& c1.hasDecoration(TextDecoration.UNDERLINED) == settings.underlined
			&& c1.hasDecoration(TextDecoration.STRIKETHROUGH) == settings.strikethrough
			&& c1.hasDecoration(TextDecoration.OBFUSCATED) == settings.obfuscated;
	}

	private static void sortSections(ArrayList<FormattedSection> sections) {
		sections.sort(
			(s1, s2) -> s1.Start == s2.Start
				? s1.End == s2.End ? 0 : Integer.compare(s1.End, s2.End)
				: Integer.compare(s1.Start, s2.Start));
	}

	private static void header(String message) {
		DebugCommand.SendDebugMessage("\n--------\n" + message + "\n--------\n");
	}
}
