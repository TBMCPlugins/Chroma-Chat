package buttondevteam.chat.components.formatter.formatting;

import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class StringMatchProvider implements MatchProvider {
	private final String[] strings;
	private final FormatSettings settings;
	@Getter
	private boolean finished;
	private int nextIndex = 0;

	/**
	 * Matches the given strings in the order given
	 *
	 * @param settings The format settings
	 * @param strings  The strings to match in the correct order
	 */
	public StringMatchProvider(FormatSettings settings, String... strings) {
		this.settings = settings;
		this.strings = strings;
	}

	@Nullable
	@Override
	public FormattedSection getNextSection(String message, ArrayList<int[]> ignoredAreas, ArrayList<int[]> removedCharacters) {
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
		return new FormattedSection(settings, i, i + len - 1, new ArrayList<>(0));
	}
}
