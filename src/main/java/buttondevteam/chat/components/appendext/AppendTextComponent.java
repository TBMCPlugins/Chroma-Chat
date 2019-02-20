package buttondevteam.chat.components.appendext;

import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;
import buttondevteam.lib.architecture.IHaveConfig;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class AppendTextComponent extends Component {
	private HashMap<String, IHaveConfig> appendTexts=new HashMap<>();
	private ConfigData<String[]> helpText(IHaveConfig config) {
		return config.getData("helpText", ()->new String[0]);
	}

	@Override
	protected void enable() {
		val cs=getConfig().getConfig(); //TODO
	}

	@Override
	protected void disable() {

	}
}
