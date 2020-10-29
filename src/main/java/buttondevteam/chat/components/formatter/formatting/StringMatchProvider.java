package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.chat.commands.ucmds.admin.DebugCommand;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public class StringMatchProvider extends MatchProviderBase {
	private final String[] strings;
	@ToString.Exclude
	private final FormatSettings settings;
	private int nextIndex = 0;
	private final boolean ignoreCase;

	/**
	 * Matches the given strings in the order given
	 *
	 * @param settings The format settings
	 * @param strings  The strings to match in the correct order
	 */
	public StringMatchProvider(String name, FormatSettings settings, boolean ignoreCase, String... strings) {
		super(name);
		this.settings = settings;
		this.strings = strings;
		this.ignoreCase = ignoreCase;
		if (ignoreCase) {
			for (int i = 0; i < strings.length; i++) {
				strings[i] = strings[i].toLowerCase();
			}
		}
	}

	@Nullable
	@Override
	public FormattedSection getNextSection(String message, ArrayList<int[]> ignoredAreas, ArrayList<int[]> removedCharacters) {
		if (ignoreCase)
			message = message.toLowerCase();
		int i = -1, len = 0;
		for (String string : strings) {
			i = message.indexOf(string, nextIndex);
			len = string.length();
			if (i != -1) break;
		}
		if (i == -1) {
			finished = true; //Won't find any more
			return null;
		}
		nextIndex = i + len;
		if (ChatFormatUtils.isInRange(i, i + len - 1, ignoredAreas)) {
			DebugCommand.SendDebugMessage("String is in ignored area, skipping");
			return null; //Not setting finished to true, so it will go to the next match
		}
		DebugCommand.SendDebugMessage("Found string match from " + i + " to " + (i + len - 1));
		DebugCommand.SendDebugMessage("With settings: " + settings);
		ChatFormatUtils.sendMessageWithPointer(message, i, i + len - 1);
		ignoredAreas.add(new int[]{i, i + len - 1});
		return new FormattedSection(settings, i, i + len - 1, Collections.emptyList());
	}

	@Override
	public void resetSubclass() {
		nextIndex = 0;
	}
}
