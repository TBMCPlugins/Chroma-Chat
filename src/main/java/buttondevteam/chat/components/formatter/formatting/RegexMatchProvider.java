package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ToString
public class RegexMatchProvider extends MatchProviderBase {
	private final Pattern pattern;
	@ToString.Exclude
	private final FormatSettings settings;
	private Matcher matcher;

	public RegexMatchProvider(String name, Pattern pattern, FormatSettings settings) {
		super(name);
		this.pattern = pattern;
		this.settings = settings;
	}

	@Nullable
	@Override
	public FormattedSection getNextSection(String message, ArrayList<int[]> ignoredAreas, ArrayList<int[]> removedCharacters) {
		if (matcher == null)
			matcher = pattern.matcher(message);
		if (!matcher.find()) {
			finished = true;
			return null;
		}
		int start = matcher.start(), end = matcher.end() - 1;
		DebugCommand.SendDebugMessage("Found regex match from " + start + " to " + end);
		DebugCommand.SendDebugMessage("With settings: " + settings);
		ChatFormatUtils.sendMessageWithPointer(message, start, end);
		if (ChatFormatUtils.isInRange(start, end, ignoredAreas)) {
			DebugCommand.SendDebugMessage("Formatter is in ignored area, skipping");
			return null; //Not setting finished to true, so it will go to the next match
		}
		ArrayList<String> groups = new ArrayList<>();
		for (int i = 0; i < matcher.groupCount(); i++)
			groups.add(matcher.group(i + 1));
		if (groups.size() > 0)
			DebugCommand.SendDebugMessage("First group: " + groups.get(0));
		return new FormattedSection(settings, matcher.start(), matcher.end() - 1, groups);
	}
}
