package buttondevteam.chat.components.formatter.formatting;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class RangeMatchProvider implements MatchProvider {
	private final String pattern;
	private final FormatSettings settings;
	@Getter
	private boolean finished;
	private int nextIndex = 0;
	private FormattedSection startedSection;

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
			startedSection = new FormattedSection(settings, i, i + len - 1, new ArrayList<>(0));
			return null;
		} else {
			startedSection.End = i + len - 1;
			return startedSection;
		}
	}
}
