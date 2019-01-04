package buttondevteam.chat.components;

import buttondevteam.lib.architecture.Component;

public class TownyComponent extends Component { //TODO: Register component
	@Override
	protected void enable() {
		TownyAnnouncer.setup();
	}

	@Override
	protected void disable() {
		TownyAnnouncer.setdown();
	}
}
