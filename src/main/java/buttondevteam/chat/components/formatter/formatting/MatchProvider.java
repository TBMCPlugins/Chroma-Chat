package buttondevteam.chat.components.formatter.formatting;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Attempts to find a match for the provided message, returning null if none was found.
 */
public interface MatchProvider {
	@Nullable
	FormattedSection getNextSection(String message, ArrayList<int[]> ignoredAreas, ArrayList<int[]> removedCharacters);

	boolean isFinished();

	String getName();

	@Override
	String toString();

	void reset();
}
