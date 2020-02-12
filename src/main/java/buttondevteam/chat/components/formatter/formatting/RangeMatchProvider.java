package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;

@ToString
public class RangeMatchProvider extends MatchProviderBase {
	private final String pattern;
	@ToString.Exclude
	private final FormatSettings settings;
	private int nextIndex = 0;
	private FormattedSection startedSection;

	public RangeMatchProvider(String name, String pattern, FormatSettings settings) {
		super(name);
		this.pattern = pattern;
		this.settings = settings;
	}

	@SuppressWarnings("DuplicatedCode")
	@Override
	public FormattedSection getNextSection(String message, ArrayList<int[]> ignoredAreas, ArrayList<int[]> removedCharacters) {
		int i, len;
		do {
			i = message.indexOf(pattern, nextIndex);
			len = pattern.length();
			nextIndex = i + len; //Set for the next loop if it's escaped or for the next method call
		} while (i > 0 && message.charAt(i - 1) == '\\');
		if (i == -1) {
			finished = true; //Won't find any more - unfinished sections will be garbage collected
			return null;
		}
		removedCharacters.add(new int[]{i, i + len - 1});
		if (startedSection == null) {
			DebugCommand.SendDebugMessage("Started range match from " + i + " to " + (i + len - 1));
			DebugCommand.SendDebugMessage("With settings: " + settings);
			ChatFormatUtils.sendMessageWithPointer(message, i, i + len - 1);
			startedSection = new FormattedSection(settings, i, i + len - 1, Collections.emptyList());
			return null;
		} else {
			DebugCommand.SendDebugMessage("Finished range match from " + i + " to " + (i + len - 1));
			DebugCommand.SendDebugMessage("With settings: " + settings);
			ChatFormatUtils.sendMessageWithPointer(message, i, i + len - 1);
			startedSection.End = i + len - 1;
			return startedSection;
		}
	}
}
