package buttondevteam.chat.components;

import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;

public class TownColorComponent extends Component {
	public ConfigData<Byte> colorCount() { //TODO
		return getData("colorCount", (byte) 1, cc -> (byte) cc, cc -> (int) cc);
	}

	public ConfigData<Boolean> useNationColors() { //TODO
		return getData("useNationColors", true);
	}

	@Override
	protected void enable() {
		//TODO: Don't register all commands automatically (welp)
	}

	@Override
	protected void disable() {

	}
}
