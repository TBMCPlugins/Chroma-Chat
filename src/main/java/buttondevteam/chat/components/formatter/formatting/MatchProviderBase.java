package buttondevteam.chat.components.formatter.formatting;

import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.architecture.IHaveConfig;
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

	ConfigData<Boolean> enabled(IHaveConfig config) {
		return config.getData(name + ".enabled", true);
	}

}
