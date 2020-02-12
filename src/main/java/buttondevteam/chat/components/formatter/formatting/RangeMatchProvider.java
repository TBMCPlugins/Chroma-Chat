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
		i = message.indexOf(pattern, nextIndex);
		len = pattern.length();
		nextIndex = i + len; //Set for the next method call
		if (i == -1) {
			finished = true; //Won't find any more - unfinished sections will be garbage collected
			return null;
		}
		if (ChatFormatUtils.isInRange(i, i + len - 1, ignoredAreas)) {
			DebugCommand.SendDebugMessage("Range start is in ignored area, skipping");
			return null; //Not setting finished to true, so it will go to the next match
		}
		ignoredAreas.add(new int[]{i, i + len - 1});
		if (startedSection == null) {
			DebugCommand.SendDebugMessage("Started range match from " + i + " to " + (i + len - 1));
			DebugCommand.SendDebugMessage("With settings: " + settings);
			ChatFormatUtils.sendMessageWithPointer(message, i, i + len - 1);
			startedSection = new FormattedSection(settings, i, i + len - 1, Collections.emptyList());
			return null;
		} else {
			var section = startedSection;
			DebugCommand.SendDebugMessage("Finished range match from " + section.Start + " to " + (i + len - 1));
			DebugCommand.SendDebugMessage("With settings: " + settings);
			ChatFormatUtils.sendMessageWithPointer(message, section.Start, i + len - 1);
			section.End = i + len - 1;
			removedCharacters.add(new int[]{section.Start, section.Start + len - 1});
			removedCharacters.add(new int[]{i, i + len - 1});
			startedSection = null; //Reset so next find creates a new one
			return section;
		}
	}

	@Override
	public void resetSubclass() {
		nextIndex = 0;
		startedSection = null;
	}
}
