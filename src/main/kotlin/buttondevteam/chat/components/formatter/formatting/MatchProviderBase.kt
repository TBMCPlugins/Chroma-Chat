package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.lib.architecture.IHaveConfig;
import buttondevteam.lib.architecture.config.IConfigData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;

@RequiredArgsConstructor
public abstract class MatchProviderBase implements MatchProvider {
	@Getter
	protected boolean finished;
	@Getter
	private final String name;

	@Nullable
	@Override
	public abstract FormattedSection getNextSection(String message, ArrayList<int[]> ignoredAreas, ArrayList<int[]> removedCharacters);

	@Override
	public String toString() {
		return name;
	}

	protected abstract void resetSubclass();

	public void reset() {
		finished = false;
		resetSubclass();
	}

	IConfigData<Boolean> enabled(IHaveConfig config) {
		return config.getData(name + ".enabled", true);
	}

}
