package buttondevteam.chat.components.fun;

import buttondevteam.lib.architecture.Component;
import lombok.val;

public class FunComponent extends Component {
	@Override
	protected void enable() {
		val pc = new PressCommand();
		registerCommand(pc);
		registerListener(pc);
	}

	@Override
	protected void disable() {

	}
}
