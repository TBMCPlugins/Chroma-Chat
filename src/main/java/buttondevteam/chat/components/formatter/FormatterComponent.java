package buttondevteam.chat.components.formatter;

import buttondevteam.chat.PluginMain;
import buttondevteam.core.ComponentManager;
import buttondevteam.core.MainPlugin;
import buttondevteam.lib.TBMCChatEvent;
import buttondevteam.lib.architecture.Component;
import buttondevteam.lib.architecture.ConfigData;

/**
 * This component handles the custom processing of chat messages. If this component is disabled channels won't be supported either in Minecraft.
 * If you only want to disable the formatting features, set allowFormatting to false.
 */
public class FormatterComponent extends Component<PluginMain> {
	ConfigData<Boolean> allowFormatting() {
		return getConfig().getData("allowFormatting", true);
	}

	@Override
	protected void enable() {
		MainPlugin.Instance.setChatHandlerEnabled(false); //Disable Core chat handler - if this component is disabled then let it do it's job
	}

	@Override
	protected void disable() {
		MainPlugin.Instance.setChatHandlerEnabled(true);
	}

	/**
	 * Handles the chat if the component is enabled.
	 *
	 * @param event The chat event
	 * @return Whether the chat message shouldn't be sent for some reason
	 */
	public static boolean handleChat(TBMCChatEvent event) {
		FormatterComponent component = ComponentManager.getIfEnabled(FormatterComponent.class);
		if (component == null) return false;
		return ChatProcessing.ProcessChat(event, component);
	}
}
